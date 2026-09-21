package com.eyrax.apothicarsenal.registry;

import com.eyrax.apothicarsenal.weapon.WeaponProfile;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;

/**
 * Phase 1 bridge. EYRAX groups are internal; activating distinct registered categories
 * requires corresponding affix and gem datasets first. A new LootCategory does not inherit melee bonuses.
 * The supplied Apotheosis data map binds the integrated tag to this native category.
 */
public final class EyraxLootCategories {
    public static LootCategory baseline(WeaponProfile profile) { return Apoth.LootCategories.MELEE_WEAPON; }
    private EyraxLootCategories() {}
}
