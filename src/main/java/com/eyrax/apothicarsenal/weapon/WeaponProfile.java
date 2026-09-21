package com.eyrax.apothicarsenal.weapon;

/** Read-only classification. No ItemStack components or attributes are modified. */
public record WeaponProfile(WeaponFamily family, WeaponGroup group, WeaponTier tier,
                            boolean conflictingTags) {}
