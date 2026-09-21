package com.eyrax.apothicarsenal.test;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.sweenus.simplytooltips.client.render.ItemThemeRegistry;
import java.nio.file.Files;
import java.nio.file.Path;

/** Development-only smoke test. Never packaged in the installable addon. */
@EventBusSubscriber(modid = "eyrax_apothic_arsenal", value = Dist.CLIENT)
public final class TooltipClientProbe {
    private static int stage = 0, ticks = 0;
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) throws Exception {
        if (!Boolean.getBoolean("eyrax.tooltipProbe")) return;
        var mc = Minecraft.getInstance();
        if (stage == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            int disabled = 0;
            for (var item : BuiltInRegistries.ITEM) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                if (!id.getNamespace().equals("simplyswords")) continue;
                if (ItemThemeRegistry.isEnabledForStack(new ItemStack(item))) throw new AssertionError("Modern panel still enabled: " + id);
                disabled++;
            }
            if (disabled != 140) throw new AssertionError("Unexpected item count: " + disabled);
            if (!ItemThemeRegistry.isEnabledForStack(new ItemStack(Items.DIAMOND_SWORD))) throw new AssertionError("Vanilla route modified");
            mc.setScreen(new Preview());
            verifyPages(mc);
            stage = 1;
        } else if (stage == 1 && ++ticks == 30) {
            Screenshot.grab(mc.gameDirectory, "eyrax-normal-page-1.png", mc.getMainRenderTarget(),
                message -> LogUtils.getLogger().info("[EYRAX Tooltip Probe] {}", message.getString()));
        } else if (stage == 1 && ticks == 40) {
            var key = new net.neoforged.neoforge.client.event.ScreenEvent.KeyPressed.Pre(mc.screen, org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN, 0, 0);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(key);
            if (!key.isCanceled()) throw new AssertionError("Preview has no navigable pages");
        } else if (stage == 1 && ticks == 50) {
            Screenshot.grab(mc.gameDirectory, "eyrax-normal-page-2.png", mc.getMainRenderTarget(), message -> {});
            Files.writeString(Path.of("tooltip-pages-result.txt"), "PASS: 140 standard routes; lossless pagination for 3 weapon tiers; forward/backward navigation; short tooltip unchanged; vanilla unaffected.\n");
            LogUtils.getLogger().info("[EYRAX Tooltip Pages] PASS: routes, pagination and navigation");
        } else if (stage == 1 && ticks > 70) {
            mc.stop();
            stage = 2;
        }
    }
    private static void verifyPages(Minecraft mc) {
        for (String id : new String[]{"shadowsting", "iron_rapier", "runic_rapier"}) {
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("simplyswords:" + id)));
            var source = new java.util.ArrayList<com.mojang.datafixers.util.Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>>();
            source.add(com.mojang.datafixers.util.Either.left(Component.literal("Title")));
            for (int i = 0; i < 40; i++) source.add(com.mojang.datafixers.util.Either.left(Component.literal("Line " + i).withStyle(net.minecraft.ChatFormatting.GOLD)));
            var collected = new java.util.ArrayList<String>();
            String first = null;
            for (int i = 0; i < 3; i++) {
                var event = new net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents(stack, 640, 360, new java.util.ArrayList<>(source), -1);
                com.eyrax.apothicarsenal.client.StandardTooltipPages.gather(event);
                var out = event.getTooltipElements();
                if (i == 0) first = out.get(1).left().orElseThrow().getString();
                for (var entry : out.subList(1, out.size() - 1)) collected.add(entry.left().orElseThrow().getString());
                var key = new net.neoforged.neoforge.client.event.ScreenEvent.KeyPressed.Pre(mc.screen, org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN, 0, 0);
                com.eyrax.apothicarsenal.client.StandardTooltipPages.key(key);
                if (!key.isCanceled()) throw new AssertionError("Page key ignored");
            }
            if (!collected.equals(java.util.stream.IntStream.range(0, 40).mapToObj(i -> "Line " + i).toList())) throw new AssertionError("Lost/reordered lines: " + collected);
            var back = new net.neoforged.neoforge.client.event.ScreenEvent.KeyPressed.Pre(mc.screen, org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP, 0, 0);
            com.eyrax.apothicarsenal.client.StandardTooltipPages.key(back);
            var last = new net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents(stack, 640, 360, new java.util.ArrayList<>(source), -1);
            com.eyrax.apothicarsenal.client.StandardTooltipPages.gather(last);
            if (!last.getTooltipElements().get(1).left().orElseThrow().getString().equals("Line 36")) throw new AssertionError("Backward navigation failed");
            var shortSource = java.util.List.of(source.get(0), source.get(1));
            var shortEvent = new net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents(stack, 640, 360, shortSource, -1);
            com.eyrax.apothicarsenal.client.StandardTooltipPages.gather(shortEvent);
            if (!shortEvent.getTooltipElements().equals(shortSource)) throw new AssertionError("Short tooltip changed");
        }
    }
    private static final class Preview extends Screen {
        private final ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("simplyswords:shadowsting")));
        Preview() {
            super(Component.literal("EYRAX tooltip preview"));
            dev.shadowsoffire.apotheosis.socket.SocketHelper.setSockets(stack, 3);
        }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF20242D);
            graphics.drawString(font, "EYRAX - Normal tooltip / pages", 16, 16, 0xFFFFFF);
            graphics.drawString(font, "Shadowsting - preview without a loaded world", 16, 32, 0xAAB8CC);
            graphics.renderItem(stack, 30, 65);
            graphics.renderTooltip(font, stack, 65, 65);
        }
    }
}
