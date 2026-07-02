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
 * 左端: バフアイコン（半透明ダークパネル上に金縁アイコン）
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

    // ── バフ表示 ──
    private static final int BUF_ICON = 18;
    private static final int BUF_W = 88;
    private static final int BUF_H = 22;

    private void renderBuffColumn(GuiGraphics g, int sh) {
        int x = 4, y = sh / 2 - 44, gap = 24;
        int f = ClientDeckState.getFocusStacks();
        int gd = ClientDeckState.getGoodTicks();
        int gr = ClientDeckState.getGreatTicks();
        if (f > 0) { drawBuff(g, x, y, GuiTextures.ICON_FOCUS, "x" + f); y += gap; }
        if (gd > 0) { drawBuff(g, x, y, GuiTextures.ICON_GOOD_CONDITION, fmt(gd)); y += gap; }
        if (gr > 0) { drawBuff(g, x, y, GuiTextures.ICON_GREAT_CONDITION, fmt(gr)); }
    }

    private void drawBuff(GuiGraphics g, int x, int y, ResourceLocation icon, String val) {
        // 半透明ダークパネル（簡易角丸再現）
        g.fill(x + 1, y, x + BUF_W - 1, y + BUF_H, 0xCC181230);
        g.fill(x, y + 1, x + BUF_W, y + BUF_H - 1, 0xCC181230);
        // 上端に薄いゴールドライン
        g.fill(x + 4, y + 1, x + BUF_W - 4, y + 2, 0x60D4A843);
        // アイコン
        int iy = y + (BUF_H - BUF_ICON) / 2;
        int tex = GuiTextures.BUFF_ICON_TEX;
        g.blit(icon, x + 4, iy, BUF_ICON, BUF_ICON, 0f, 0f, tex, tex, tex, tex);
        // テキスト
        g.drawString(Minecraft.getInstance().font, Component.literal(val),
                x + 4 + BUF_ICON + 6, y + BUF_H / 2 - 4, 0xFFFFFFFF, true);
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
