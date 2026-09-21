package com.eyrax.apothicarsenal.command;

import com.eyrax.apothicarsenal.compat.SocketingDiagnosis;
import com.eyrax.apothicarsenal.weapon.WeaponClassifier;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ArsenalCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eyrax_arsenal")
            .then(Commands.literal("diagnose").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                var weapon = player.getMainHandItem();
                var gem = player.getOffhandItem();
                var result = SocketingDiagnosis.inspect(weapon, gem);
                String details = "Arma: " + BuiltInRegistries.ITEM.getKey(weapon.getItem())
                    + " | Gema: " + BuiltInRegistries.ITEM.getKey(gem.getItem())
                    + " | Categoría: " + LootCategory.forItem(weapon).getKey()
                    + " | Sockets Apotheosis: " + SocketHelper.getSockets(weapon)
                    + " | Gemas válidas insertadas: " + SocketHelper.getGems(weapon).streamValidGems().count();
                context.getSource().sendSuccess(() -> Component.literal(details), false);
                WeaponClassifier.classify(weapon).ifPresent(profile -> context.getSource().sendSuccess(
                    () -> Component.literal("Familia: " + profile.family() + " | Grupo EYRAX: " + profile.group() + " | Tier: " + profile.tier()), false));
                String message = switch (result) {
                    case EMPTY_WEAPON -> "Sostén el arma en la mano principal y la gema en la secundaria.";
                    case INVALID_GEM -> "La mano secundaria no contiene una gema válida de Apotheosis.";
                    case NO_APOTHEOSIS_SOCKET -> "El arma no tiene sockets de Apotheosis. Los sockets de Simply Swords son independientes.";
                    case NO_EMPTY_SOCKET -> "No queda un socket vacío de Apotheosis.";
                    case INVALID_CATEGORY -> "Apotheosis no reconoce una categoría válida para esta arma.";
                    case UNSUPPORTED_BONUS -> "Esta gema no tiene un bono compatible con la categoría del arma y su pureza.";
                    case UNIQUE_GEM_DUPLICATE -> "Esta gema es única y ya hay una del mismo tipo en el arma.";
                    case SERVER_POLICY -> "La configuración EYRAX del servidor o su tag de bloqueo impide otra inserción.";
                    case EVENT_BLOCKED -> "Un evento de otro mod bloquea la inserción; revisar los mods del pack.";
                    case READY -> "La gema es compatible. Usa la mesa de herrería: plantilla vacía, arma en base y gema en adición.";
                };
                context.getSource().sendSuccess(() -> Component.literal("[" + result + "] " + message), false);
                if (result == SocketingDiagnosis.Result.READY) {
                    var input = new SmithingRecipeInput(ItemStack.EMPTY, weapon, gem);
                    var recipe = player.serverLevel().getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, player.serverLevel());
                    context.getSource().sendSuccess(() -> Component.literal("Receta seleccionada por el servidor: "
                        + recipe.map(holder -> holder.id().toString()).orElse("NINGUNA: revisar datapacks/recetas")), false);
                }
                return 1;
            })));
    }
    private ArsenalCommands() {}
}
