package com.idolcraft.client.gui;

import com.idolcraft.card.CardDefinition;
import com.idolcraft.card.CardRegistry;
import com.idolcraft.client.ClientDeckState;
import com.idolcraft.util.PLevelCurve;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * プロデューサーランク（Pレベル）画面 ─ 学マス風の白ベース＋すりガラスUI。
 * 上部に現在のPレベルと経験値バー、下部にPレベルで解放されるカードの一覧（解放/未解放）を表示する。
 */
public class PLevelScreen extends Screen {

    // ── レイアウト ──
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 320;
    private static final int HEADER_W = 300;
    private static final int HEADER_H = 74;
    private static final int ROW_H = 26;
    private static final int LIST_VISIBLE_ROWS = 6;

    // ── カラー（学マス風 白ベース + CMYアクセント）──
    private static final int COLOR_TITLE    = 0xFF3A3550;
    private static final int COLOR_CYAN     = 0xFF3AB2EC;
    private static final int COLOR_PINK     = 0xFFF26098;
    private static final int COLOR_LABEL    = 0xFF6B6880;
    private static final int COLOR_SUB      = 0xFF8A87A0;
    private static final int COLOR_BACKDROP = 0x88283050;
    private static final int COLOR_BAR_BG   = 0xFFE4E7F2;
    private static final int COLOR_BAR_FILL = 0xFF3AB2EC;
    private static final int COLOR_ROW_BG   = 0xFFF6F8FD;
    private static final int COLOR_ROW_LOCK = 0xFFECEEF5;
    private static final int COLOR_UNLOCKED = 0xFF33A65B; // 解放済み（緑）
    private static final int COLOR_LOCKED   = 0xFF9A97AC; // 未解放（グレー）

    /** Pレベルで解放されるカード（requiredPLevel > 0）を解放レベル昇順で保持 */
    private final List<CardDefinition> lockedCards = new ArrayList<>();

    private int panelX, panelY;
    private int scroll = 0;

    public PLevelScreen() {
        super(Component.literal("プロデューサーランク"));
    }

    @Override
    protected void init() {
        super.init();
        lockedCards.clear();
        for (CardDefinition def : CardRegistry.all()) {
            if (def.getRequiredPLevel() > 0) lockedCards.add(def);
        }
        lockedCards.sort(Comparator.comparingInt(CardDefinition::getRequiredPLevel)
                .thenComparing(CardDefinition::getDisplayName));

        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - PANEL_H) / 2;
        this.scroll = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, COLOR_BACKDROP);

        // ── パネル ──
        blitTex(g, GuiTextures.PANEL, panelX, panelY, PANEL_W, PANEL_H,
                GuiTextures.PANEL_TEX_W, GuiTextures.PANEL_TEX_H);

        // ── ヘッダー ──
        int hx = panelX + (PANEL_W - HEADER_W) / 2;
        int hy = panelY - 18;
        blitTex(g, GuiTextures.HEADER, hx, hy, HEADER_W, HEADER_H,
                GuiTextures.HEADER_TEX_W, GuiTextures.HEADER_TEX_H);
        g.drawCenteredString(this.font, "プロデューサーランク", panelX + PANEL_W / 2, hy + HEADER_H / 2 - 5, COLOR_TITLE);

        renderRankBlock(g);
        renderCardList(g, mouseX, mouseY);
    }

    private void renderRankBlock(GuiGraphics g) {
        long xp = ClientDeckState.getProduceXp();
        int level = PLevelCurve.levelForXp(xp);
        boolean max = PLevelCurve.isMaxLevel(xp);

        int cx = panelX + PANEL_W / 2;
        int topY = panelY + 58;

        // 大きな「P Lv.N」表示（フォントを2倍に拡大）
        String big = "P Lv." + level;
        var pose = g.pose();
        pose.pushPose();
        pose.translate(cx, topY, 0);
        pose.scale(2.0f, 2.0f, 1f);
        g.drawString(this.font, big, -this.font.width(big) / 2, 0, COLOR_PINK, false);
        pose.popPose();

        // 経験値バー
        int barW = 250;
        int barH = 12;
        int barX = cx - barW / 2;
        int barY = topY + 26;
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFFC7CBDC); // 枠
        g.fill(barX, barY, barX + barW, barY + barH, COLOR_BAR_BG);

        String barLabel;
        if (max) {
            g.fill(barX, barY, barX + barW, barY + barH, COLOR_BAR_FILL);
            barLabel = "MAX";
        } else {
            long into = PLevelCurve.xpIntoLevel(xp);
            long need = PLevelCurve.xpForNext(xp);
            float ratio = need > 0 ? Math.min(1f, (float) into / need) : 0f;
            int fillW = Math.round(barW * ratio);
            if (fillW > 0) g.fill(barX, barY, barX + fillW, barY + barH, COLOR_BAR_FILL);
            barLabel = into + " / " + need + " EXP";
        }
        g.drawCenteredString(this.font, barLabel, cx, barY + barH + 4, COLOR_SUB);

        // 総経験値・説明
        g.drawCenteredString(this.font, "累計経験値 " + xp + "（経験値オーブ取得で上昇）",
                cx, barY + barH + 16, COLOR_LABEL);
    }

    private void renderCardList(GuiGraphics g, int mouseX, int mouseY) {
        int level = ClientDeckState.getPLevel();
        int listX = panelX + 24;
        int listW = PANEL_W - 48;
        int listY = panelY + 132;

        g.drawString(this.font, "解放カード", listX, listY - 12, COLOR_LABEL, false);

        int maxScroll = Math.max(0, lockedCards.size() - LIST_VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int row = 0; row < LIST_VISIBLE_ROWS; row++) {
            int idx = scroll + row;
            if (idx >= lockedCards.size()) break;
            CardDefinition def = lockedCards.get(idx);
            int y = listY + row * ROW_H;
            boolean unlocked = level >= def.getRequiredPLevel();

            // 行の背景（角丸なしのフラット。解放済みは白、未解放は少し暗い）
            g.fill(listX, y, listX + listW, y + ROW_H - 4, unlocked ? COLOR_ROW_BG : COLOR_ROW_LOCK);
            // 左端に状態色のバー（解放=緑 / 未解放=グレー）
            g.fill(listX, y, listX + 3, y + ROW_H - 4, unlocked ? COLOR_UNLOCKED : COLOR_LOCKED);

            // カードアイテムアイコン
            Item item = ForgeRegistries.ITEMS.getValue(def.getId());
            if (item != null) {
                g.renderItem(new ItemStack(item), listX + 8, y + 2);
            }

            // カード名（未解放は暗め）
            int nameColor = unlocked ? COLOR_TITLE : COLOR_LOCKED;
            g.drawString(this.font, def.getDisplayName(), listX + 30, y + 6, nameColor, false);

            // 右側に解放レベル/状態
            String right = unlocked ? "解放済み" : "PLv." + def.getRequiredPLevel();
            int rColor = unlocked ? COLOR_UNLOCKED : COLOR_LOCKED;
            int rw = this.font.width(right);
            g.drawString(this.font, right, listX + listW - rw - 6, y + 6, rColor, false);
        }

        // スクロールヒント
        if (maxScroll > 0) {
            g.drawCenteredString(this.font, "▲▼ スクロールで全" + lockedCards.size() + "枚",
                    panelX + PANEL_W / 2, listY + LIST_VISIBLE_ROWS * ROW_H + 2, COLOR_SUB);
        }
    }

    private void blitTex(GuiGraphics g, ResourceLocation tex, int x, int y, int dw, int dh, int tw, int th) {
        GuiTextures.bindSmooth(tex);
        g.blit(tex, x, y, dw, dh, 0f, 0f, tw, th, tw, th);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll -= (int) Math.signum(delta);
        int maxScroll = Math.max(0, lockedCards.size() - LIST_VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        return true;
    }

    @Override public boolean isPauseScreen() { return false; }
}

