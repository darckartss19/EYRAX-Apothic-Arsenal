package com.eyrax.apothicarsenal.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Uses the normal renderer, including third-party colors and tooltip components. */
@EventBusSubscriber(modid = "eyrax_apothic_arsenal", value = Dist.CLIENT)
public final class StandardTooltipPages {
    private static ItemStack hovered = ItemStack.EMPTY;
    private static Screen screen;
    private static int page, pageCount;
    private static long lastRender;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void gather(RenderTooltipEvent.GatherComponents event) {
        var mc = Minecraft.getInstance();
        var stack = event.getItemStack();
        if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("simplyswords")) {
            pageCount = 0;
            return;
        }
        long now = System.nanoTime();
        if (screen != mc.screen || now - lastRender > 300_000_000L || !ItemStack.isSameItemSameComponents(hovered, stack)) page = 0;
        screen = mc.screen;
        hovered = stack.copy();
        lastRender = now;
        var elements = event.getTooltipElements();
        pageCount = 0;
        if (elements.size() < 2) return;
        // Keep enough room on either side of the pointer to avoid a second wrapping pass.
        int width = Math.max(40, Math.min(300, event.getScreenWidth() / 2 - 24));
        if (event.getMaxWidth() > 0) width = Math.min(width, event.getMaxWidth());
        var header = wrap(List.of(elements.getFirst()), width);
        var body = wrap(new ArrayList<>(elements.subList(1, elements.size())), width);
        int headerHeight = header.stream().mapToInt(StandardTooltipPages::height).sum();
        int budget = Math.max(10, Math.min(180, event.getScreenHeight() - headerHeight - 46));
        var pages = TooltipPagination.partition(body, budget, StandardTooltipPages::height);
        if (pages.size() < 2) return;
        pageCount = pages.size();
        page = Math.floorMod(page, pageCount);
        elements.clear();
        elements.addAll(header);
        elements.addAll(pages.get(page));
        elements.add(Either.left(Component.translatable("tooltip.eyrax_apothic_arsenal.page", page + 1, pageCount)
            .withStyle(ChatFormatting.GRAY)));
        event.setMaxWidth(width);
    }

    private static List<Either<FormattedText, TooltipComponent>> wrap(List<Either<FormattedText, TooltipComponent>> source, int width) {
        var result = new ArrayList<Either<FormattedText, TooltipComponent>>();
        var font = Minecraft.getInstance().font;
        for (var entry : source) {
            if (entry.left().isPresent()) {
                var lines = font.getSplitter().splitLines(entry.left().get(), width, Style.EMPTY);
                if (lines.isEmpty()) result.add(entry);
                else for (var line : lines) result.add(Either.left(line));
            } else result.add(entry);
        }
        return result;
    }
    private static int height(Either<FormattedText, TooltipComponent> entry) {
        return entry.map(text -> 10, component -> ClientTooltipComponent.create(component).getHeight());
    }

    @SubscribeEvent
    public static void key(ScreenEvent.KeyPressed.Pre event) {
        if (pageCount < 2 || event.getScreen() != screen || System.nanoTime() - lastRender > 300_000_000L
            || screen.getFocused() instanceof EditBox) return;
        int direction = switch (event.getKeyCode()) {
            case GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_PAGE_DOWN -> 1;
            case GLFW.GLFW_KEY_PAGE_UP -> -1;
            default -> 0;
        };
        if (direction != 0) {
            page = Math.floorMod(page + direction, pageCount);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void scroll(ScreenEvent.MouseScrolled.Pre event) {
        if (pageCount < 2 || event.getScreen() != screen || System.nanoTime() - lastRender > 300_000_000L
            || event.getScrollDeltaY() == 0) return;
        page = Math.floorMod(page + (event.getScrollDeltaY() > 0 ? -1 : 1), pageCount);
        event.setCanceled(true);
    }
    private StandardTooltipPages() {}
}
