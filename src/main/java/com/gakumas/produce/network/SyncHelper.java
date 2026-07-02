package com.gakumas.produce.network;

import com.gakumas.produce.capability.IDeckData;
import com.gakumas.produce.network.packet.SyncDeckPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class SyncHelper {
    private SyncHelper() {}

    public static void syncTo(ServerPlayer player, IDeckData deck) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncDeckPacket(deck));
    }
}
