package com.gakumas.produce.network;

import com.gakumas.produce.capability.IDeckData;
import com.gakumas.produce.network.packet.SyncDeckPacket;
import com.gakumas.produce.network.packet.SyncOwnedCardsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class SyncHelper {
    private SyncHelper() {}

    public static void syncTo(ServerPlayer player, IDeckData deck) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDeckPacket(player, deck));
    }

    /** 習得済みカードのコレクションを同期する（習得時・ログイン時のみ呼ぶ） */
    public static void syncOwned(ServerPlayer player, IDeckData deck) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncOwnedCardsPacket(deck));
    }
}
