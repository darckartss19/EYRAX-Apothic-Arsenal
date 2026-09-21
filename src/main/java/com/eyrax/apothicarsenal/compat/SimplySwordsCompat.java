package com.eyrax.apothicarsenal.compat;

import com.eyrax.apothicarsenal.registry.EyraxTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;

public final class SimplySwordsCompat {
    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(EyraxTags.ALL)) return true;
        // Future untagged swords still appear in diagnostics, without importing private weapon classes.
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("simplyswords")
            && stack.canPerformAction(ItemAbilities.SWORD_DIG);
    }
    private SimplySwordsCompat() {}
}
