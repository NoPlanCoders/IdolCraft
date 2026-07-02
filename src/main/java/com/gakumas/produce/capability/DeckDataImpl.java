package com.gakumas.produce.capability;

import com.gakumas.produce.buff.BuffState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DeckDataImpl implements IDeckData {

    private final List<ResourceLocation> drawPile = new ArrayList<>();
    private final List<ResourceLocation> hand = new ArrayList<>();
    private final List<ResourceLocation> discardPile = new ArrayList<>();
    private final List<ResourceLocation> exclusionPile = new ArrayList<>();
    private final List<ResourceLocation> masterCardList = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean initialized = false;
    private final BuffState buffState = new BuffState();

    @Override public List<ResourceLocation> getDrawPile() { return drawPile; }
    @Override public List<ResourceLocation> getHand() { return hand; }
    @Override public List<ResourceLocation> getDiscardPile() { return discardPile; }
    @Override public List<ResourceLocation> getExclusionPile() { return exclusionPile; }

    @Override public int getSelectedIndex() { return selectedIndex; }
    @Override public void setSelectedIndex(int index) {
        if (hand.isEmpty()) { this.selectedIndex = 0; return; }
        int size = hand.size();
        this.selectedIndex = ((index % size) + size) % size;
    }

    @Override public BuffState getBuffState() { return buffState; }

    @Override public List<ResourceLocation> getMasterCardList() { return masterCardList; }
    @Override public void setMasterCardList(List<ResourceLocation> cards) {
        masterCardList.clear();
        masterCardList.addAll(cards);
    }

    @Override public boolean isInitialized() { return initialized; }
    @Override public void setInitialized(boolean initialized) { this.initialized = initialized; }

    private static ListTag toListTag(List<ResourceLocation> list) {
        ListTag tag = new ListTag();
        for (ResourceLocation rl : list) tag.add(StringTag.valueOf(rl.toString()));
        return tag;
    }

    private static List<ResourceLocation> fromListTag(ListTag tag) {
        List<ResourceLocation> list = new ArrayList<>();
        for (Tag t : tag) {
            ResourceLocation rl = ResourceLocation.tryParse(t.getAsString());
            if (rl != null) list.add(rl);
        }
        return list;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("draw", toListTag(drawPile));
        tag.put("hand", toListTag(hand));
        tag.put("discard", toListTag(discardPile));
        tag.put("exclusion", toListTag(exclusionPile));
        tag.put("master", toListTag(masterCardList));
        tag.putInt("selected", selectedIndex);
        tag.putBoolean("initialized", initialized);
        tag.put("buff", buffState.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        drawPile.clear(); drawPile.addAll(fromListTag(tag.getList("draw", Tag.TAG_STRING)));
        hand.clear(); hand.addAll(fromListTag(tag.getList("hand", Tag.TAG_STRING)));
        discardPile.clear(); discardPile.addAll(fromListTag(tag.getList("discard", Tag.TAG_STRING)));
        exclusionPile.clear(); exclusionPile.addAll(fromListTag(tag.getList("exclusion", Tag.TAG_STRING)));
        masterCardList.clear(); masterCardList.addAll(fromListTag(tag.getList("master", Tag.TAG_STRING)));
        selectedIndex = tag.getInt("selected");
        initialized = tag.getBoolean("initialized");
        buffState.deserializeNBT(tag.getCompound("buff"));
    }
}
