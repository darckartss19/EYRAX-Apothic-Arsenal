package com.eyrax.apothicarsenal.weapon;

public enum WeaponTier {
    STANDARD, RUNIC, UNIQUE, UNKNOWN;

    public static WeaponTier fromTags(boolean unique, boolean runic, boolean standard) {
        return unique ? UNIQUE : runic ? RUNIC : standard ? STANDARD : UNKNOWN;
    }
}
