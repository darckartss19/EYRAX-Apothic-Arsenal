package com.eyrax.apothicarsenal.compat;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import net.minecraft.world.item.ItemStack;

/** Exposes the actual Apotheosis checks; does not force acceptance or touch either socket system. */
public final class SocketingDiagnosis {
    public enum Result {
        EMPTY_WEAPON, INVALID_GEM, NO_APOTHEOSIS_SOCKET, NO_EMPTY_SOCKET,
        INVALID_CATEGORY, UNSUPPORTED_BONUS, UNIQUE_GEM_DUPLICATE,
        SERVER_POLICY, EVENT_BLOCKED, READY
    }
    public static Result inspect(ItemStack weapon, ItemStack gemStack) {
        if (weapon.isEmpty()) return Result.EMPTY_WEAPON;
        var gem = UnsocketedGem.of(gemStack);
        if (!gem.isValid()) return Result.INVALID_GEM;
        if (SocketHelper.getSockets(weapon) == 0) return Result.NO_APOTHEOSIS_SOCKET;
        if (!SocketHelper.hasEmptySockets(weapon)) return Result.NO_EMPTY_SOCKET;
        var category = LootCategory.forItem(weapon);
        if (category.isNone()) return Result.INVALID_CATEGORY;
        if (!gem.gem().get().isValidIn(weapon, gemStack, gem.purity())) return Result.UNSUPPORTED_BONUS;
        if (!gem.canApplyTo(weapon)) return Result.UNIQUE_GEM_DUPLICATE;
        if (SocketingPolicy.blocks(weapon)) return Result.SERVER_POLICY;
        if (!SocketHelper.canSocketGemInItem(weapon, gemStack)) return Result.EVENT_BLOCKED;
        return Result.READY;
    }
    private SocketingDiagnosis() {}
}
