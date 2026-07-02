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
    private static int selectedIndex = 0;
    private static int focusStacks = 0;
    private static int goodTicks = 0;
    private static int greatTicks = 0;

    private ClientDeckState() {}

    public static void update(List<ResourceLocation> newHand, int selected, int focus, int good, int great) {
        hand = newHand;
        selectedIndex = selected;
        focusStacks = focus;
        goodTicks = good;
        greatTicks = great;
    }

    /** クライアント側描画の滑らかさのため、サーバー同期の合間も毎クライアントTickで1ずつ減らす（サーバー再同期で補正される） */
    public static void localTick() {
        if (goodTicks > 0) goodTicks--;
        if (greatTicks > 0) greatTicks--;
    }

    public static List<ResourceLocation> getHand() { return hand; }
    public static int getSelectedIndex() { return selectedIndex; }
    public static int getFocusStacks() { return focusStacks; }
    public static int getGoodTicks() { return goodTicks; }
    public static int getGreatTicks() { return greatTicks; }
}
