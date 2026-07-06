package com.idolcraft.network;

import com.idolcraft.capability.IDeckData;
import com.idolcraft.network.packet.SyncDeckPacket;
import com.idolcraft.network.packet.SyncOwnedCardsPacket;
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

