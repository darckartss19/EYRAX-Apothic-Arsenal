package com.eyrax.apothicarsenal.weapon;

import com.eyrax.apothicarsenal.compat.SimplySwordsCompat;
import com.eyrax.apothicarsenal.registry.EyraxTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

public final class WeaponClassifier {
    public static Optional<WeaponProfile> classify(ItemStack stack) {
        if (!SimplySwordsCompat.isWeapon(stack)) return Optional.empty();
        WeaponFamily family = WeaponFamily.UNKNOWN;
        int familyMatches = 0;
        for (WeaponFamily candidate : WeaponFamily.values()) {
            if (candidate != WeaponFamily.UNKNOWN && stack.is(EyraxTags.family(candidate))) {
                family = candidate;
                familyMatches++;
            }
        }
        if (familyMatches > 1) family = WeaponFamily.UNKNOWN;
        if (familyMatches == 0 && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("simplyswords")) {
            family = WeaponFamily.fromItemPath(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
        }
        WeaponGroup group = family.group();
        int groupMatches = 0;
        for (WeaponGroup candidate : WeaponGroup.values()) {
            if (candidate != WeaponGroup.UNASSIGNED && stack.is(EyraxTags.group(candidate))) {
                group = candidate;
                groupMatches++;
            }
        }
        if (groupMatches > 1) group = WeaponGroup.UNASSIGNED;
        WeaponTier tier = WeaponTier.fromTags(stack.is(EyraxTags.UNIQUE), stack.is(EyraxTags.RUNIC), stack.is(EyraxTags.STANDARD));
        return Optional.of(new WeaponProfile(family, group, tier, familyMatches > 1 || groupMatches > 1));
    }
    private WeaponClassifier() {}
}
