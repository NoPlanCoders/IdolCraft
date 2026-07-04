package com.gakumas.produce.network.packet;

import com.gakumas.produce.capability.IDeckData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * サーバー → クライアント：習得済みカードのコレクションを同期する。
 * コレクションは変化が稀（習得時・ログイン時）なため、頻繁に送る {@link SyncDeckPacket} とは分けている。
 */
public class SyncOwnedCardsPacket {

    private final List<ResourceLocation> owned;

    public SyncOwnedCardsPacket(IDeckData deck) {
        this.owned = new ArrayList<>(deck.getOwnedCards());
    }

    private SyncOwnedCardsPacket(List<ResourceLocation> owned) {
        this.owned = owned;
    }

    public static void encode(SyncOwnedCardsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.owned.size());
        for (ResourceLocation id : msg.owned) buf.writeResourceLocation(id);
    }

    public static SyncOwnedCardsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> owned = new ArrayList<>();
        for (int i = 0; i < size; i++) owned.add(buf.readResourceLocation());
        return new SyncOwnedCardsPacket(owned);
    }

    public static void handle(SyncOwnedCardsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.gakumas.produce.client.ClientDeckState.updateOwned(msg.owned)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}
