package com.gakumas.produce.network.packet;

import com.gakumas.produce.capability.IDeckData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncDeckPacket {

    private final List<ResourceLocation> hand;
    private final int selectedIndex;
    private final int focusStacks;
    private final int goodTicks;
    private final int greatTicks;

    public SyncDeckPacket(IDeckData deck) {
        this.hand = new ArrayList<>(deck.getHand());
        this.selectedIndex = deck.getSelectedIndex();
        this.focusStacks = deck.getBuffState().getFocusStacks();
        this.goodTicks = deck.getBuffState().getGoodConditionTicks();
        this.greatTicks = deck.getBuffState().getGreatConditionTicks();
    }

    private SyncDeckPacket(List<ResourceLocation> hand, int selectedIndex, int focusStacks, int goodTicks, int greatTicks) {
        this.hand = hand;
        this.selectedIndex = selectedIndex;
        this.focusStacks = focusStacks;
        this.goodTicks = goodTicks;
        this.greatTicks = greatTicks;
    }

    public static void encode(SyncDeckPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hand.size());
        for (ResourceLocation rl : msg.hand) buf.writeResourceLocation(rl);
        buf.writeVarInt(msg.selectedIndex);
        buf.writeVarInt(msg.focusStacks);
        buf.writeVarInt(msg.goodTicks);
        buf.writeVarInt(msg.greatTicks);
    }

    public static SyncDeckPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> hand = new ArrayList<>();
        for (int i = 0; i < size; i++) hand.add(buf.readResourceLocation());
        int selected = buf.readVarInt();
        int focus = buf.readVarInt();
        int good = buf.readVarInt();
        int great = buf.readVarInt();
        return new SyncDeckPacket(hand, selected, focus, good, great);
    }

    public static void handle(SyncDeckPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.gakumas.produce.client.ClientDeckState.update(msg.hand, msg.selectedIndex, msg.focusStacks, msg.goodTicks, msg.greatTicks)
                )
        );
        ctx.get().setPacketHandled(true);
    }
}
