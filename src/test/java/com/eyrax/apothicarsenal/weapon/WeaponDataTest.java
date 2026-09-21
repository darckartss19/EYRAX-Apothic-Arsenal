package com.eyrax.apothicarsenal.weapon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Checks the complete released corpus and the actual packaged tag graph, without booting Minecraft. */
class WeaponDataTest {
    private static final Path DATA = Path.of("src/main/resources/data");
    private static JsonObject read(Path path) throws Exception {
        try (var reader = Files.newBufferedReader(path)) { return JsonParser.parseReader(reader).getAsJsonObject(); }
    }
    private static Set<String> entries(JsonObject tag) {
        Set<String> values = new HashSet<>();
        for (var entry : tag.getAsJsonArray("values")) {
            values.add(entry.isJsonPrimitive() ? entry.getAsString() : entry.getAsJsonObject().get("id").getAsString());
        }
        return values;
    }
    @Test void corpusHasNoLostOrMultiplyAssignedFamilies() throws Exception {
        var corpus = read(Path.of("docs/weapon-inventory.json"));
        var owners = new HashMap<String, String>();
        for (WeaponFamily family : WeaponFamily.values()) {
            if (family == WeaponFamily.UNKNOWN) continue;
            var tag = read(DATA.resolve("eyrax_apothic_arsenal/tags/item/families/" + family.path() + ".json"));
            for (String id : entries(tag)) {
                if (!id.startsWith("#")) assertNull(owners.put(id, family.path()), "Duplicate family for " + id);
            }
        }
        int unknown = 0;
        for (var element : corpus.getAsJsonArray("items")) {
            var row = element.getAsJsonObject();
            String id = row.get("item").getAsString(), family = row.get("family").getAsString();
            if (family.equals("unknown")) {
                unknown++;
                assertEquals("simplyswords:decaying_relic", id);
                assertEquals("unique", row.get("tier").getAsString());
            } else assertEquals(family, owners.get(id), id);
        }
        assertEquals(134, corpus.getAsJsonArray("items").size());
        assertEquals(1, unknown);
        assertEquals(61, corpus.getAsJsonObject("tiers").get("standard").getAsInt());
        assertEquals(15, corpus.getAsJsonObject("tiers").get("runic").getAsInt());
        assertEquals(58, corpus.getAsJsonObject("tiers").get("unique").getAsInt());
    }
    @Test void dataMapUsesNativeCategoryAndPreservesOtherPacks() throws Exception {
        var map = read(DATA.resolve("apotheosis/data_maps/item/loot_category_overrides.json"));
        assertFalse(map.get("replace").getAsBoolean());
        assertEquals("apotheosis:melee_weapon", map.getAsJsonObject("values")
            .get("#eyrax_apothic_arsenal:weapons/integrated").getAsString());
        try (var paths = Files.walk(DATA.resolve("eyrax_apothic_arsenal/tags/item"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                var tag = read(path);
                assertFalse(tag.get("replace").getAsBoolean());
                for (String entry : entries(tag)) {
                    if (entry.startsWith("#eyrax_apothic_arsenal:")) {
                        assertTrue(Files.exists(DATA.resolve("eyrax_apothic_arsenal/tags/item/"
                            + entry.substring(entry.indexOf(':') + 1) + ".json")), entry);
                    }
                }
            }
        }
    }
    @Test void handlesUpstreamTierTagExceptions() throws Exception {
        var standard = entries(read(DATA.resolve("eyrax_apothic_arsenal/tags/item/weapons/standard.json")));
        assertTrue(standard.contains("simplyswords:sword_on_a_stick"));
        assertFalse(standard.contains("simplyswords:dreadtide"));
        var runic = entries(read(DATA.resolve("eyrax_apothic_arsenal/tags/item/weapons/runic.json")));
        assertTrue(runic.contains("#simplyswords:runic_gear"));
        assertFalse(runic.contains("#simplyswords:runic_weapons"));
    }
}
