package com.gakumas.produce.network.packet;

import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.capability.IDeckData;
import com.gakumas.produce.card.CardRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncDeckPacket {

    private final List<ResourceLocation> hand;
    private final List<Boolean> handUsable;
    private final int selectedIndex;
    private final int focusStacks;
    private final int goodTicks;
    private final int greatTicks;
    private final int pLevel;
    private final long produceXp;

    public SyncDeckPacket(ServerPlayer player, IDeckData deck) {
        this.hand = new ArrayList<>(deck.getHand());
        this.handUsable = new ArrayList<>();
        for (ResourceLocation cardId : hand) {
            boolean usable = CardRegistry.get(cardId)
                    .map(def -> DeckService.isUsable(player, deck, def))
                    .orElse(true);
            this.handUsable.add(usable);
        }
        this.selectedIndex = deck.getSelectedIndex();
        this.focusStacks = deck.getBuffState().getFocusStacks();
        this.goodTicks = deck.getBuffState().getGoodConditionTicks();
        this.greatTicks = deck.getBuffState().getGreatConditionTicks();
        this.pLevel = deck.getPLevel();
        this.produceXp = deck.getProduceXp();
    }

    private SyncDeckPacket(List<ResourceLocation> hand, List<Boolean> handUsable, int selectedIndex, int focusStacks, int goodTicks, int greatTicks, int pLevel, long produceXp) {
        this.hand = hand;
        this.handUsable = handUsable;
        this.selectedIndex = selectedIndex;
        this.focusStacks = focusStacks;
        this.goodTicks = goodTicks;
        this.greatTicks = greatTicks;
        this.pLevel = pLevel;
        this.produceXp = produceXp;
    }

    public static void encode(SyncDeckPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hand.size());
        for (ResourceLocation rl : msg.hand) buf.writeResourceLocation(rl);
        for (boolean usable : msg.handUsable) buf.writeBoolean(usable);
        buf.writeVarInt(msg.selectedIndex);
        buf.writeVarInt(msg.focusStacks);
        buf.writeVarInt(msg.goodTicks);
        buf.writeVarInt(msg.greatTicks);
        buf.writeVarInt(msg.pLevel);
        buf.writeVarLong(msg.produceXp);
    }

    public static SyncDeckPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> hand = new ArrayList<>();
        for (int i = 0; i < size; i++) hand.add(buf.readResourceLocation());
        List<Boolean> handUsable = new ArrayList<>();
        for (int i = 0; i < size; i++) handUsable.add(buf.readBoolean());
        int selected = buf.readVarInt();
        int focus = buf.readVarInt();
        int good = buf.readVarInt();
        int great = buf.readVarInt();
        int pLevel = buf.readVarInt();
        long produceXp = buf.readVarLong();
        return new SyncDeckPacket(hand, handUsable, selected, focus, good, great, pLevel, produceXp);
    }

    public static void handle(SyncDeckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.gakumas.produce.client.ClientDeckState.update(msg.hand, msg.handUsable, msg.selectedIndex, msg.focusStacks, msg.goodTicks, msg.greatTicks, msg.pLevel, msg.produceXp)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}
