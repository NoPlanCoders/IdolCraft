package com.gakumas.produce.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

/**
 * サーバーから同期された、HUD描画専用のクライアント側キャッシュ。
 * 好調/絶好調の残りTickはサーバーからの同期後、クライアント側でも毎Tickカウントダウン表示するため
 * {@link #localTick()} で補間する。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientDeckState {

    private static List<ResourceLocation> hand = Collections.emptyList();
    private static List<Boolean> handUsable = Collections.emptyList();
    private static int selectedIndex = 0;
    private static int focusStacks = 0;
    private static int goodTicks = 0;
    private static int greatTicks = 0;
    private static int pLevel = 0;
    private static long produceXp = 0;
    private static List<ResourceLocation> ownedCards = Collections.emptyList();

    private ClientDeckState() {}

    /** 習得済みカードのコレクションを更新する（SyncOwnedCardsPacket 受信時） */
    public static void updateOwned(List<ResourceLocation> owned) {
        ownedCards = owned;
    }

    public static List<ResourceLocation> getOwnedCards() { return ownedCards; }

    public static void update(List<ResourceLocation> newHand, List<Boolean> newHandUsable, int selected, int focus, int good, int great, int level, long xp) {
        hand = newHand;
        handUsable = newHandUsable;
        selectedIndex = selected;
        focusStacks = focus;
        goodTicks = good;
        greatTicks = great;
        pLevel = level;
        produceXp = xp;
    }

    /** クライアント側描画の滑らかさのため、サーバー同期の合間も毎クライアントTickで1ずつ減らす（サーバー再同期で補正される） */
    public static void localTick() {
        if (goodTicks > 0) goodTicks--;
        if (greatTicks > 0) greatTicks--;
    }

    public static List<ResourceLocation> getHand() { return hand; }

    /** 指定した手札インデックスのカードが「今この瞬間」選択・発動できるか（進捗/Pレベル/使用条件込み） */
    public static boolean isHandUsable(int index) {
        return index < 0 || index >= handUsable.size() || handUsable.get(index);
    }

    public static int getSelectedIndex() { return selectedIndex; }
    public static int getFocusStacks() { return focusStacks; }
    public static int getGoodTicks() { return goodTicks; }
    public static int getGreatTicks() { return greatTicks; }
    public static int getPLevel() { return pLevel; }
    public static long getProduceXp() { return produceXp; }
}
