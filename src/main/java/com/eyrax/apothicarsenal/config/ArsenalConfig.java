package com.eyrax.apothicarsenal.config;

import com.eyrax.apothicarsenal.weapon.WeaponTier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server policy for new insertions only. Existing components and gem effects are never rewritten. */
public final class ArsenalConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue STANDARD_GEMS;
    public static final ModConfigSpec.IntValue RUNIC_GEMS;
    public static final ModConfigSpec.IntValue UNIQUE_GEMS;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.comment("Limits on NEW Apotheosis gem insertions. -1 preserves Apotheosis rules; 0 blocks new insertions.",
            "Does not change Simply Swords sockets, existing gems, affixes, or attack speed.")
            .push("socketing");
        STANDARD_GEMS = builder.defineInRange("standardMaxInsertedGems", -1, -1, 16);
        RUNIC_GEMS = builder.defineInRange("runicMaxInsertedGems", -1, -1, 16);
        UNIQUE_GEMS = builder.defineInRange("uniqueMaxInsertedGems", -1, -1, 16);
        builder.pop();
        SPEC = builder.build();
    }
    public static int limit(WeaponTier tier) {
        return switch (tier) {
            case STANDARD -> STANDARD_GEMS.get();
            case RUNIC -> RUNIC_GEMS.get();
            case UNIQUE -> UNIQUE_GEMS.get();
            case UNKNOWN -> -1;
        };
    }
    public static boolean allows(int limit, long inserted) { return limit < 0 || inserted < limit; }
    private ArsenalConfig() {}
}
