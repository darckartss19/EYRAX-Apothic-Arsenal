package com.eyrax.apothicarsenal.compat;

import com.eyrax.apothicarsenal.registry.EyraxLootCategories;
import com.eyrax.apothicarsenal.weapon.WeaponProfile;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import net.minecraft.world.item.ItemStack;

public final class ApotheosisCompat {
    public static boolean isValidAffixCandidate(ItemStack stack) { return !LootCategory.forItem(stack).isNone(); }
    public static boolean hasExpectedCategory(ItemStack stack, WeaponProfile profile) {
        return LootCategory.forItem(stack) == EyraxLootCategories.baseline(profile);
    }
    private ApotheosisCompat() {}
}
