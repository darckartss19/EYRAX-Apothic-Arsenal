package com.eyrax.apothicarsenal.weapon;

public enum WeaponGroup {
    EYRAX_SWIFT("swift"), EYRAX_HEAVY("heavy"), EYRAX_POLEARM("polearm"),
    EYRAX_SPECIAL("special"), UNASSIGNED("unassigned");

    private final String path;
    WeaponGroup(String path) { this.path = path; }
    public String path() { return path; }
}
