package com.eyrax.apothicarsenal.weapon;

import java.util.Locale;
import static com.eyrax.apothicarsenal.weapon.WeaponGroup.*;

public enum WeaponFamily {
    RAPIER(EYRAX_SWIFT), CUTLASS(EYRAX_SWIFT), SAI(EYRAX_SWIFT), DAGGER(EYRAX_SWIFT),
    CLAYMORE(EYRAX_HEAVY), LONGSWORD(UNASSIGNED), GREATHAMMER(EYRAX_HEAVY), HAMMER(EYRAX_HEAVY),
    KATANA(EYRAX_SWIFT), SPEAR(EYRAX_POLEARM), GLAIVE(EYRAX_POLEARM), HALBERD(EYRAX_POLEARM),
    WARGLAIVE(EYRAX_SWIFT), CHAKRAM(EYRAX_SPECIAL), SCYTHE(EYRAX_SPECIAL), GREATAXE(EYRAX_HEAVY),
    TWINBLADE(EYRAX_SWIFT), UNKNOWN(UNASSIGNED);

    private final WeaponGroup group;
    WeaponFamily(WeaponGroup group) { this.group = group; }
    public WeaponGroup group() { return group; }
    public String path() { return name().toLowerCase(Locale.ROOT); }

    /** Conservative fallback for future registry entries; never guess from substrings. */
    public static WeaponFamily fromItemPath(String path) {
        for (WeaponFamily family : values()) {
            if (family != UNKNOWN && (path.equals(family.path()) || path.endsWith("_" + family.path()))) {
                return family;
            }
        }
        return UNKNOWN;
    }
}
