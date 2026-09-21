package com.eyrax.apothicarsenal.compat;

import com.eyrax.apothicarsenal.config.ArsenalConfig;
import com.eyrax.apothicarsenal.registry.EyraxTags;
import com.eyrax.apothicarsenal.weapon.WeaponClassifier;
import dev.shadowsoffire.apotheosis.event.CanSocketGemEvent;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import net.minecraft.world.item.ItemStack;

public final class SocketingPolicy {
    public static boolean blocks(ItemStack stack) {
        var profile = WeaponClassifier.classify(stack);
        if (profile.isEmpty()) return false;
        if (stack.is(EyraxTags.SOCKETING_BLOCKED)) return true;
        int limit = ArsenalConfig.limit(profile.get().tier());
        return !ArsenalConfig.allows(limit, SocketHelper.getGems(stack).streamValidGems().count());
    }
    public static void onCanSocket(CanSocketGemEvent event) {
        if (blocks(event.getInputStack())) event.setCanceled(true);
    }
    private SocketingPolicy() {}
}
