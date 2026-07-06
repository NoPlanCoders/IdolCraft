package com.idolcraft.capability;

import com.idolcraft.buff.BuffState;
import com.idolcraft.util.PLevelCurve;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DeckDataImpl implements IDeckData {

    private final List<ResourceLocation> drawPile = new ArrayList<>();
    private final List<ResourceLocation> hand = new ArrayList<>();
    private final List<ResourceLocation> discardPile = new ArrayList<>();
    private final List<ResourceLocation> exclusionPile = new ArrayList<>();
    private final List<ResourceLocation> masterCardList = new ArrayList<>();
    private final Set<ResourceLocation> ownedCards = new LinkedHashSet<>();
    private int selectedIndex = 0;
    private boolean initialized = false;
    private long produceXp = 0;
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

    @Override public int getPLevel() { return PLevelCurve.levelForXp(produceXp); }
    @Override public void setPLevel(int level) { this.produceXp = PLevelCurve.totalXpForLevel(Math.max(1, level)); }

    @Override public long getProduceXp() { return produceXp; }
    @Override public void setProduceXp(long xp) { this.produceXp = Math.max(0, xp); }
    @Override public void addProduceXp(long delta) { this.produceXp = Math.max(0, this.produceXp + delta); }

    @Override public Set<ResourceLocation> getOwnedCards() { return ownedCards; }
    @Override public boolean addOwnedCard(ResourceLocation cardId) { return ownedCards.add(cardId); }
    @Override public boolean hasOwnedCard(ResourceLocation cardId) { return ownedCards.contains(cardId); }

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
        tag.putLong("produceXp", produceXp);
        tag.put("owned", toListTag(new ArrayList<>(ownedCards)));
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
        // 経験値制へ移行: 新形式 produceXp があればそれを使い、旧形式(pLevel)しかなければ換算して引き継ぐ
        if (tag.contains("produceXp")) {
            produceXp = tag.getLong("produceXp");
        } else if (tag.contains("pLevel")) {
            produceXp = PLevelCurve.totalXpForLevel(Math.max(1, tag.getInt("pLevel")));
        } else {
            produceXp = 0;
        }
        ownedCards.clear();
        ownedCards.addAll(fromListTag(tag.getList("owned", Tag.TAG_STRING)));
        buffState.deserializeNBT(tag.getCompound("buff"));
    }
}

