package com.eyrax.apothicarsenal.weapon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeaponTaxonomyTest {
    @Test void avoidsAmbiguousSuffixesAndSubstringGuesses() {
        assertEquals(WeaponFamily.GREATHAMMER, WeaponFamily.fromItemPath("netherite_greathammer"));
        assertEquals(WeaponFamily.WARGLAIVE, WeaponFamily.fromItemPath("runic_warglaive"));
        assertEquals(WeaponFamily.UNKNOWN, WeaponFamily.fromItemPath("rapier_gem"));
        assertEquals(WeaponFamily.UNKNOWN, WeaponFamily.fromItemPath("future_weapon"));
    }
    @Test void uniqueWinsEvenWhenAlsoInRunicTag() {
        assertEquals(WeaponTier.UNIQUE, WeaponTier.fromTags(true, true, false));
        assertEquals(WeaponTier.RUNIC, WeaponTier.fromTags(false, true, true));
        assertEquals(WeaponTier.UNKNOWN, WeaponTier.fromTags(false, false, false));
    }
    @Test void preservesUnspecifiedLongswordGroup() {
        assertEquals(WeaponGroup.UNASSIGNED, WeaponFamily.LONGSWORD.group());
        assertEquals(WeaponGroup.EYRAX_POLEARM, WeaponFamily.SPEAR.group());
    }
}
