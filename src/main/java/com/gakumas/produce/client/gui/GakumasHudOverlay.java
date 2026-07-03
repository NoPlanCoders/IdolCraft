package com.gakumas.produce.client.gui;

import com.gakumas.produce.client.ClientDeckState;
import com.gakumas.produce.item.HandbookItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;

/**
 * 手帳所持中のHUDオーバーレイ v2。
 * 左端: バフアイコン（本家学マスの実アイコンをそのまま表示）
 * 下部: 手札（選択中カードはゴールド発光＋呼吸アニメーション）
 */
public class GakumasHudOverlay implements IGuiOverlay {

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_TEX = GuiTextures.SLOT_TEX;
    private static final float SLOT_MARGIN_RATIO = 48f / 312f;

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics g, float pt, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        renderBuffColumn(g, sh);
        if (!(mc.player.getMainHandItem().getItem() instanceof HandbookItem)
                && !(mc.player.getOffhandItem().getItem() instanceof HandbookItem)) return;
        renderHand(mc, g, sw, sh);
    }

    // ── バフ表示（本家学マス風: 浮遊ダイヤ + 横にテキスト） ──
    private static final int BUF_ICON = 18;

    private void renderBuffColumn(GuiGraphics g, int sh) {
        int x = 6, gap = 23;
        int y = sh / 2 - 34;
        int f = ClientDeckState.getFocusStacks();
        int gd = ClientDeckState.getGoodTicks();
        int gr = ClientDeckState.getGreatTicks();
        if (f > 0) { drawBuff(g, x, y, GuiTextures.ICON_FOCUS, "x" + f); y += gap; }
        if (gd > 0) { drawBuff(g, x, y, GuiTextures.ICON_GOOD_CONDITION, fmt(gd)); y += gap; }
        if (gr > 0) { drawBuff(g, x, y, GuiTextures.ICON_GREAT_CONDITION, fmt(gr)); }
    }

    private void drawBuff(GuiGraphics g, int x, int y, ResourceLocation icon, String val) {
        // ダイヤ型アイコン（テクスチャ自体に影・白リムを含む。縮小はリニアで滑らかに）
        int tex = GuiTextures.BUFF_ICON_TEX;
        GuiTextures.bindSmooth(icon);
        g.blit(icon, x, y, BUF_ICON, BUF_ICON, 0f, 0f, tex, tex, tex, tex);
        // テキスト（白＋影で世界背景に対して視認性確保）
        g.drawString(Minecraft.getInstance().font, Component.literal(val),
                x + BUF_ICON + 3, y + (BUF_ICON - 8) / 2, 0xFFFFFFFF, true);
    }

    private static String fmt(int ticks) {
        return String.format(Locale.US, "%.1fs", ticks / 20.0);
    }

    // ── 手札表示 ──
    private void renderHand(Minecraft mc, GuiGraphics g, int sw, int sh) {
        List<ResourceLocation> hand = ClientDeckState.getHand();
        if (hand.isEmpty()) return;

        int spacing = 4;
        int tw = hand.size() * SLOT_SIZE + (hand.size() - 1) * spacing;
        int sx = sw / 2 - tw / 2;
        int baseY = sh - 80;
        int sel = ClientDeckState.getSelectedIndex();
        float pulse = 1.0f + 0.05f * (0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 1000.0 * 2.4));

        for (int i = 0; i < hand.size(); i++) {
            int x = sx + i * (SLOT_SIZE + spacing);
            int y = baseY - (i == sel ? 8 : 0);

            var pose = g.pose();
            pose.pushPose();
            if (i == sel) {
                float cx = x + SLOT_SIZE / 2f;
                float cy = y + SLOT_SIZE / 2f;
                pose.translate(cx, cy, 0);
                pose.scale(pulse, pulse, 1f);
                pose.translate(-cx, -cy, 0);
            }

            ResourceLocation slotTex = (i == sel) ? GuiTextures.SLOT_HOVER : GuiTextures.SLOT;
            GuiTextures.bindSmooth(slotTex);
            g.blit(slotTex, x, y, SLOT_SIZE, SLOT_SIZE, 0f, 0f, SLOT_TEX, SLOT_TEX, SLOT_TEX, SLOT_TEX);

            Item item = ForgeRegistries.ITEMS.getValue(hand.get(i));
            if (item != null) {
                g.renderItem(new ItemStack(item), x + (SLOT_SIZE - 16) / 2, y + (SLOT_SIZE - 16) / 2);
            }
            if (!ClientDeckState.isHandUsable(i)) {
                int inset = Math.round(SLOT_SIZE * SLOT_MARGIN_RATIO);
                g.fill(x + inset, y + inset, x + SLOT_SIZE - inset, y + SLOT_SIZE - inset, 0xB0101018);
            }
            pose.popPose();
        }
    }
}
