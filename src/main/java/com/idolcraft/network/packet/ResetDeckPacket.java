package com.idolcraft.network.packet;

import com.idolcraft.capability.DeckCapability;
import com.idolcraft.capability.DeckService;
import com.idolcraft.network.SyncHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetDeckPacket {

    public ResetDeckPacket() {}

    public static void encode(ResetDeckPacket msg, FriendlyByteBuf buf) {}

    public static ResetDeckPacket decode(FriendlyByteBuf buf) {
        return new ResetDeckPacket();
    }

    public static void handle(ResetDeckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
                DeckService.resetDeck(player, deck);
                SyncHelper.syncTo(player, deck);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

