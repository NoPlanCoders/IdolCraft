package com.gakumas.produce.network.packet;

import com.gakumas.produce.capability.DeckCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * クライアント → サーバー：デッキ構成を設定するパケット
 * デッキエディター画面から「確定」ボタンを押した際に送信される
 */
public class SetDeckPacket {
    private final List<ResourceLocation> cardIds;

    public SetDeckPacket(List<ResourceLocation> cardIds) {
        this.cardIds = new ArrayList<>(cardIds);
    }

    public static void encode(SetDeckPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.cardIds.size());
        for (ResourceLocation id : msg.cardIds) {
            buf.writeResourceLocation(id);
        }
    }

    public static SetDeckPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> cardIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cardIds.add(buf.readResourceLocation());
        }
        return new SetDeckPacket(cardIds);
    }

    public static void handle(SetDeckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
                deck.setMasterCardList(msg.cardIds);
                player.displayClientMessage(
                        Component.literal("【デッキ構成を保存しました】 (" + msg.cardIds.size() + "枚)")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
