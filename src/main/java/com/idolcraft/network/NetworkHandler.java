package com.idolcraft.network;

import com.idolcraft.IdolCraft;
import com.idolcraft.network.packet.ResetDeckPacket;
import com.idolcraft.network.packet.SelectCardPacket;
import com.idolcraft.network.packet.CraftCardPacket;
import com.idolcraft.network.packet.SetDeckPacket;
import com.idolcraft.network.packet.SyncDeckPacket;
import com.idolcraft.network.packet.SyncOwnedCardsPacket;
import com.idolcraft.network.packet.UseOrSkipPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(IdolCraft.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++, SelectCardPacket.class, SelectCardPacket::encode, SelectCardPacket::decode, SelectCardPacket::handle);
        CHANNEL.registerMessage(id++, UseOrSkipPacket.class, UseOrSkipPacket::encode, UseOrSkipPacket::decode, UseOrSkipPacket::handle);
        CHANNEL.registerMessage(id++, ResetDeckPacket.class, ResetDeckPacket::encode, ResetDeckPacket::decode, ResetDeckPacket::handle);
        CHANNEL.registerMessage(id++, SyncDeckPacket.class, SyncDeckPacket::encode, SyncDeckPacket::decode, SyncDeckPacket::handle);
        CHANNEL.registerMessage(id++, SetDeckPacket.class, SetDeckPacket::encode, SetDeckPacket::decode, SetDeckPacket::handle);
        CHANNEL.registerMessage(id++, SyncOwnedCardsPacket.class, SyncOwnedCardsPacket::encode, SyncOwnedCardsPacket::decode, SyncOwnedCardsPacket::handle);
        CHANNEL.registerMessage(id++, CraftCardPacket.class, CraftCardPacket::encode, CraftCardPacket::decode, CraftCardPacket::handle);
    }
}

