package com.eyrax.apothicarsenal;

import com.eyrax.apothicarsenal.compat.ApotheosisCompat;
import com.eyrax.apothicarsenal.weapon.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import com.eyrax.apothicarsenal.config.ArsenalConfig;
import com.eyrax.apothicarsenal.command.ArsenalCommands;
import com.eyrax.apothicarsenal.compat.SocketingPolicy;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.slf4j.Logger;
import java.util.EnumMap;

@Mod(EyraxApothicArsenal.MOD_ID)
public final class EyraxApothicArsenal {
    public static final String MOD_ID = "eyrax_apothic_arsenal";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "[EYRAX Apothic Arsenal] ";

    public EyraxApothicArsenal(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ArsenalConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(ArsenalCommands::register);
        NeoForge.EVENT_BUS.addListener(SocketingPolicy::onCanSocket);
        if (ModList.get().isLoaded("simplyswords")) LOGGER.info(PREFIX + "Simply Swords detected.");
        if (ModList.get().isLoaded("apotheosis")) LOGGER.info(PREFIX + "Apotheosis detected.");
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onDataMapsUpdated);
    }

    private void onServerStarted(ServerStartedEvent event) { diagnose(); }
    private void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        // Tags and category overrides are ready here, including after /reload.
        if (event.getRegistry() == BuiltInRegistries.ITEM
                && event.getCause() == DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) diagnose();
    }

    private static void diagnose() {
        var groups = new EnumMap<WeaponGroup, Integer>(WeaponGroup.class);
        var tiers = new EnumMap<WeaponTier, Integer>(WeaponTier.class);
        var families = new EnumMap<WeaponFamily, Integer>(WeaponFamily.class);
        int recognized = 0, valid = 0, baseline = 0;
        for (var item : BuiltInRegistries.ITEM) {
            var stack = new ItemStack(item);
            var result = WeaponClassifier.classify(stack);
            if (result.isEmpty()) continue;
            var profile = result.get();
            recognized++;
            groups.merge(profile.group(), 1, Integer::sum);
            tiers.merge(profile.tier(), 1, Integer::sum);
            families.merge(profile.family(), 1, Integer::sum);
            if (ApotheosisCompat.isValidAffixCandidate(stack)) valid++;
            if (ApotheosisCompat.hasExpectedCategory(stack, profile)) baseline++;
            else LOGGER.warn(PREFIX + "Unexpected Apotheosis category: {}", BuiltInRegistries.ITEM.getKey(item));
            if (profile.family() == WeaponFamily.UNKNOWN || profile.tier() == WeaponTier.UNKNOWN || profile.conflictingTags()) {
                LOGGER.warn(PREFIX + "Unclassified Simply Swords weapon: {} (family={}, tier={}, conflictingTags={})",
                    BuiltInRegistries.ITEM.getKey(item), profile.family(), profile.tier(), profile.conflictingTags());
            }
        }
        LOGGER.info(PREFIX + "Recognized {} Simply Swords weapons.", recognized);
        LOGGER.info(PREFIX + "Swift: {}", groups.getOrDefault(WeaponGroup.EYRAX_SWIFT, 0));
        LOGGER.info(PREFIX + "Heavy: {}", groups.getOrDefault(WeaponGroup.EYRAX_HEAVY, 0));
        LOGGER.info(PREFIX + "Polearm: {}", groups.getOrDefault(WeaponGroup.EYRAX_POLEARM, 0));
        LOGGER.info(PREFIX + "Special: {}", groups.getOrDefault(WeaponGroup.EYRAX_SPECIAL, 0));
        LOGGER.info(PREFIX + "Unassigned group (including longsword): {}", groups.getOrDefault(WeaponGroup.UNASSIGNED, 0));
        LOGGER.info(PREFIX + "Standard weapons: {}", tiers.getOrDefault(WeaponTier.STANDARD, 0));
        LOGGER.info(PREFIX + "Runic weapons: {}", tiers.getOrDefault(WeaponTier.RUNIC, 0));
        LOGGER.info(PREFIX + "Unique weapons: {}", tiers.getOrDefault(WeaponTier.UNIQUE, 0));
        LOGGER.info(PREFIX + "Unknown tier: {}", tiers.getOrDefault(WeaponTier.UNKNOWN, 0));
        LOGGER.info(PREFIX + "Families: {}", families);
        LOGGER.info(PREFIX + "Valid affix candidates: {}/{}; native melee category: {}/{}", valid, recognized, baseline, recognized);
    }
}
