package com.idolcraft.network.packet;

import com.idolcraft.capability.DeckCapability;
import com.idolcraft.network.SyncHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectCardPacket {
    private final int delta;

    public SelectCardPacket(int delta) {
        this.delta = delta;
    }

    public static void encode(SelectCardPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.delta);
    }

    public static SelectCardPacket decode(FriendlyByteBuf buf) {
        return new SelectCardPacket(buf.readVarInt());
    }

    public static void handle(SelectCardPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
                com.idolcraft.capability.DeckService.changeSelection(deck, msg.delta);
                SyncHelper.syncTo(player, deck);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

