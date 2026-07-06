package com.idolcraft.network.packet;

import com.idolcraft.capability.DeckCapability;
import com.idolcraft.capability.DeckService;
import com.idolcraft.network.SyncHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseOrSkipPacket {
    private final boolean skip;

    public UseOrSkipPacket(boolean skip) {
        this.skip = skip;
    }

    public static void encode(UseOrSkipPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.skip);
    }

    public static UseOrSkipPacket decode(FriendlyByteBuf buf) {
        return new UseOrSkipPacket(buf.readBoolean());
    }

    public static void handle(UseOrSkipPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            // 手帳を所持していない場合は無効
            boolean holdingHandbook = player.getMainHandItem().getItem() instanceof com.idolcraft.item.HandbookItem
                    || player.getOffhandItem().getItem() instanceof com.idolcraft.item.HandbookItem;
            if (!holdingHandbook) return;

            player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
                DeckService.performAction(player, deck, msg.skip);
                SyncHelper.syncTo(player, deck);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

