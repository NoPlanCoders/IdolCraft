package com.gakumas.produce.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.world.entity.player.Player;

public final class DeckCapability {

    public static final Capability<IDeckData> DECK_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private DeckCapability() {}

    /** 便宜メソッド：Capabilityが存在しない場合は例外を投げず空実装相当を返さず、呼び出し側でOptional処理する前提のショートカット */
    public static LazyOptional<IDeckData> get(Player player) {
        return player.getCapability(DECK_DATA);
    }
}
