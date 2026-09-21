package com.eyrax.apothicarsenal.test;

import com.eyrax.apothicarsenal.weapon.WeaponClassifier;
import com.mojang.logging.LogUtils;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.eyrax.apothicarsenal.compat.SocketingDiagnosis;
import com.eyrax.apothicarsenal.config.ArsenalConfig;
import com.eyrax.apothicarsenal.weapon.WeaponTier;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.nbt.NbtOps;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.power.GemPowerComponent;
import net.sweenus.simplyswords.registry.ComponentTypeRegistry;

@GameTestHolder("eyrax_apothic_arsenal")
@PrefixGameTestTemplate(false)
public class ArsenalGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void allWeaponsHaveMeleeCategory(GameTestHelper helper) {
        int count = 0;
        for (var item : BuiltInRegistries.ITEM) {
            var stack = new ItemStack(item);
            if (WeaponClassifier.classify(stack).isEmpty()) continue;
            count++;
            helper.assertTrue(LootCategory.forItem(stack) == Apoth.LootCategories.MELEE_WEAPON,
                "Incorrect category: " + BuiltInRegistries.ITEM.getKey(item));
        }
        helper.assertTrue(count >= 133, "Missing weapons: " + count);
        LogUtils.getLogger().info("[Arsenal GameTest] Verified {} weapon categories", count);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void gemsSocketThroughSmithingRecipe(GameTestHelper helper) {
        int combinations = 0;
        var recipe = new SocketingRecipe();
        for (var item : BuiltInRegistries.ITEM) {
            var stack = new ItemStack(item);
            if (WeaponClassifier.classify(stack).isEmpty()) continue;
            SocketHelper.setSockets(stack, 1);
            for (var gem : GemRegistry.INSTANCE.getValues()) {
                for (var purity : Purity.values()) {
                    var gemStack = gem.toStack(purity);
                    if (!UnsocketedGem.of(gemStack).canApplyTo(stack)) continue;
                    var input = new SmithingRecipeInput(ItemStack.EMPTY, stack, gemStack);
                    helper.assertTrue(recipe.matches(input, helper.getLevel()),
                        "Recipe rejected: " + BuiltInRegistries.ITEM.getKey(item) + " / " + gem.getId());
                    var selected = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
                    helper.assertTrue(selected.isPresent(), "No registered smithing recipe for " + gem.getId());
                    var actual = selected.orElseThrow().value().assemble(input, helper.getLevel().registryAccess());
                    helper.assertTrue(!actual.isEmpty() && SocketHelper.getGems(actual).streamValidGems().count() == 1,
                        "Competing smithing recipe: " + selected.orElseThrow().id());
                    var output = recipe.assemble(input, helper.getLevel().registryAccess());
                    helper.assertTrue(!output.isEmpty() && SocketHelper.getGems(output).streamValidGems().count() == 1,
                        "Invalid socketed output: " + gem.getId());
                    helper.assertTrue(SocketHelper.hasEmptySockets(stack), "Recipe mutated original weapon");
                    combinations++;
                }
            }
        }
        helper.assertTrue(combinations > 1000, "Insufficient gem matrix: " + combinations);
        LogUtils.getLogger().info("[Arsenal GameTest] Verified {} weapon/gem/purity smithing combinations", combinations);
        helper.succeed();
    }

    private static ItemStack weapon(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("simplyswords", path)));
    }
    private static ItemStack meleeGem() {
        return GemRegistry.INSTANCE.getValues().stream()
            .filter(g -> g.getBonus(Apoth.LootCategories.MELEE_WEAPON, Purity.PERFECT).isPresent())
            .findFirst().orElseThrow().toStack(Purity.PERFECT);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void preserveSimplySwordsComponentsThroughSocketReforgeSave(GameTestHelper helper) {
        int checked = 0;
        for (String id : new String[]{"iron_rapier", "runic_rapier", "bramblethorn", "netherite_greathammer"}) {
            var stack = weapon(id);
            helper.assertTrue(!stack.isEmpty(), "Missing test weapon " + id);
            SimplySwordsAPI.getOrCreateWeaponImplicit(stack);
            var implicit = stack.get(ComponentTypeRegistry.WEAPON_IMPLICIT.get());
            helper.assertTrue(implicit != null, "Implicit not initialized " + id);
            // Real component type with independently populated Runefused/Netherfused slots.
            var power = new GemPowerComponent(true, true, ResourceLocation.parse("simplyswords:freeze"),
                ResourceLocation.parse("simplyswords:berserk"));
            helper.assertTrue(!power.runic().isEmpty() && !power.nether().isEmpty(), "Test powers are not registered");
            stack.set(ComponentTypeRegistry.GEM_POWER.get(), power);
            stack.set(ComponentTypeRegistry.ADDITIONAL_GEM_SOCKETS.get(), true);
            SocketHelper.setSockets(stack, 1);
            var socketed = SocketHelper.socketGemInItem(stack, meleeGem());
            helper.assertTrue(!socketed.isEmpty(), "Failed socket insertion " + id);
            assertComponents(helper, socketed, implicit, power);
            for (var rarity : RarityRegistry.INSTANCE.getValues()) {
                var context = GenContext.standalone(RandomSource.create(42), WorldTier.PINNACLE, 0, helper.getLevel(), BlockPos.ZERO);
                var reforged = LootController.createLootItem(socketed.copy(), rarity, context);
                helper.assertTrue(reforged.getOrDefault(Apoth.Components.AFFIXES,
                    dev.shadowsoffire.apotheosis.affix.ItemAffixes.EMPTY).size() > 0, "No affixes: " + id + " / " + rarity);
                assertComponents(helper, reforged, implicit, power);
                var salvaging = helper.getLevel().getRecipeManager().getRecipeFor(Apoth.RecipeTypes.SALVAGING,
                    new SingleRecipeInput(reforged), helper.getLevel());
                helper.assertTrue(salvaging.isPresent() && !salvaging.orElseThrow().value().getOutputs().isEmpty(),
                    "No salvage recipe: " + id + " / " + rarity);
                checked++;
            }
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var encoded = ItemStack.CODEC.encodeStart(ops, socketed).getOrThrow();
            var restored = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
            assertComponents(helper, restored, implicit, power);
            helper.assertTrue(SocketHelper.getGems(restored).streamValidGems().count() == 1, "Apotheosis gem lost after save");
        }
        LogUtils.getLogger().info("[Arsenal GameTest] Verified {} reforge/salvage cases and 4 dual-system save round trips", checked);
        helper.succeed();
    }

    private static void assertComponents(GameTestHelper helper, ItemStack stack, Object implicit, Object power) {
        helper.assertTrue(implicit.equals(stack.get(ComponentTypeRegistry.WEAPON_IMPLICIT.get())), "Simply Swords implicit changed");
        helper.assertTrue(power.equals(stack.get(ComponentTypeRegistry.GEM_POWER.get())), "Simply Swords gem powers changed");
        helper.assertTrue(Boolean.TRUE.equals(stack.get(ComponentTypeRegistry.ADDITIONAL_GEM_SOCKETS.get())), "Simply Swords socket flag changed");
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void diagnosesSeparateSocketsAndInsertionLimits(GameTestHelper helper) {
        var stack = weapon("runic_rapier");
        stack.set(ComponentTypeRegistry.GEM_POWER.get(), GemPowerComponent.createEmpty(true, true));
        helper.assertTrue(SocketingDiagnosis.inspect(stack, meleeGem()) == SocketingDiagnosis.Result.NO_APOTHEOSIS_SOCKET,
            "Simply Swords socket must not count as an Apotheosis socket");
        SocketHelper.setSockets(stack, 1);
        helper.assertTrue(SocketingDiagnosis.inspect(stack, meleeGem()) == SocketingDiagnosis.Result.READY, "Compatible gem not ready");
        helper.assertTrue(SocketingDiagnosis.inspect(stack, new ItemStack(net.minecraft.world.item.Items.DIAMOND))
            == SocketingDiagnosis.Result.INVALID_GEM, "Vanilla diamond incorrectly accepted");
        var filled = SocketHelper.socketGemInItem(stack, meleeGem());
        helper.assertTrue(SocketingDiagnosis.inspect(filled, meleeGem()) == SocketingDiagnosis.Result.NO_EMPTY_SOCKET, "Full socket not detected");
        helper.assertTrue(ArsenalConfig.limit(WeaponTier.RUNIC) == -1, "Default policy unexpectedly restricts runic");
        helper.assertTrue(ArsenalConfig.allows(-1, 16) && !ArsenalConfig.allows(0, 0)
            && ArsenalConfig.allows(2, 1) && !ArsenalConfig.allows(2, 2), "Policy limit boundary failure");
        int previous = ArsenalConfig.RUNIC_GEMS.get();
        try {
            ArsenalConfig.RUNIC_GEMS.set(0);
            helper.assertTrue(SocketingDiagnosis.inspect(stack, meleeGem()) == SocketingDiagnosis.Result.SERVER_POLICY,
                "Server policy not reported");
            helper.assertTrue(!SocketHelper.canSocketGemInItem(stack, meleeGem()), "Server policy did not block actual recipe event");
            helper.assertTrue(SocketHelper.getGems(filled).streamValidGems().count() == 1,
                "Policy invalidated a previously socketed gem");
            helper.assertTrue(filled.get(ComponentTypeRegistry.GEM_POWER.get()).equals(stack.get(ComponentTypeRegistry.GEM_POWER.get())),
                "Policy changed Simply Swords slots");
        } finally {
            ArsenalConfig.RUNIC_GEMS.set(previous);
        }
        helper.succeed();
    }
}
