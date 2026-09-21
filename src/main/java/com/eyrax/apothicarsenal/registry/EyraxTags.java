package com.eyrax.apothicarsenal.registry;

import com.eyrax.apothicarsenal.EyraxApothicArsenal;
import com.eyrax.apothicarsenal.weapon.WeaponFamily;
import com.eyrax.apothicarsenal.weapon.WeaponGroup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.EnumMap;
import java.util.Map;

public final class EyraxTags {
    public static final TagKey<Item> ALL = own("weapons/all");
    public static final TagKey<Item> STANDARD = own("weapons/standard");
    public static final TagKey<Item> RUNIC = own("weapons/runic");
    public static final TagKey<Item> UNIQUE = own("weapons/unique");
    public static final TagKey<Item> INTEGRATED = own("weapons/integrated");
    public static final TagKey<Item> SOCKETING_BLOCKED = own("weapons/socketing_blocked");
    private static final Map<WeaponFamily, TagKey<Item>> FAMILIES = new EnumMap<>(WeaponFamily.class);
    private static final Map<WeaponGroup, TagKey<Item>> GROUPS = new EnumMap<>(WeaponGroup.class);
    static {
        for (WeaponFamily family : WeaponFamily.values()) {
            if (family != WeaponFamily.UNKNOWN) FAMILIES.put(family, own("families/" + family.path()));
        }
        for (WeaponGroup group : WeaponGroup.values()) {
            if (group != WeaponGroup.UNASSIGNED) GROUPS.put(group, own("weapons/" + group.path()));
        }
    }
    public static TagKey<Item> family(WeaponFamily family) { return FAMILIES.get(family); }
    public static TagKey<Item> group(WeaponGroup group) { return GROUPS.get(group); }
    public static TagKey<Item> own(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(EyraxApothicArsenal.MOD_ID, path));
    }
    private EyraxTags() {}
}
