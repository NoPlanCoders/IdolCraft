package com.idolcraft.client.gui;

import com.idolcraft.card.CardDefinition;
import com.idolcraft.card.CardRegistry;
import com.idolcraft.card.CardType;
import com.idolcraft.capability.DeckCapability;
import com.idolcraft.network.NetworkHandler;
import com.idolcraft.network.packet.SetDeckPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * デッキ編成画面 v2 ─ 学マス本家UIを強く意識したスタイリッシュなデザイン。
 * 左側: カード一覧、右側: デッキ構成。ホバーカードの詳細はカード横に大きく豪華に表示。
 */
public class DeckEditorScreen extends Screen {

    private static final ResourceLocation TEX_PANEL        = GuiTextures.PANEL;
    private static final ResourceLocation TEX_HEADER       = GuiTextures.HEADER;
    private static final ResourceLocation TEX_SLOT         = GuiTextures.SLOT;
    private static final ResourceLocation TEX_SLOT_DECK    = GuiTextures.SLOT_DECK;
    private static final ResourceLocation TEX_SLOT_HOVER   = GuiTextures.SLOT_HOVER;
    private static final ResourceLocation TEX_PILL_CONFIRM = GuiTextures.PILL_CONFIRM;
    private static final ResourceLocation TEX_PILL_RESET   = GuiTextures.PILL_RESET;
    private static final ResourceLocation TEX_PILL_CANCEL  = GuiTextures.PILL_CANCEL;

    private static final int TEXSIZE_PANEL_W  = GuiTextures.PANEL_TEX_W;
    private static final int TEXSIZE_PANEL_H  = GuiTextures.PANEL_TEX_H;
    private static final int TEXSIZE_HEADER_W = GuiTextures.HEADER_TEX_W;
    private static final int TEXSIZE_HEADER_H = GuiTextures.HEADER_TEX_H;
    private static final int TEXSIZE_SLOT     = GuiTextures.SLOT_TEX;
    private static final int TEXSIZE_PILL_W   = GuiTextures.PILL_TEX_W;
    private static final int TEXSIZE_PILL_H   = GuiTextures.PILL_TEX_H;

    // ── レイアウト ──
    private static final int PANEL_W = 470;
    private static final int PANEL_H = 350;
    private static final int HEADER_W = 310;
    private static final int HEADER_H = 74;
    private static final int SLOT_SIZE = 34;
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 4;
    private static final int MAX_DECK_SIZE = 30;

    // ── カラー（学マス風 白ベース + CMYアクセント）──
    private static final int COLOR_TITLE     = 0xFF3A3550; // ヘッダー白地に乗るタイトル（濃いスレート）
    private static final int COLOR_PINK      = 0xFFF26098; // CMY: マゼンタ（主要アクセント）
    private static final int COLOR_CYAN      = 0xFF3AB2EC; // CMY: シアン
    private static final int COLOR_YELLOW    = 0xFFF8C442; // CMY: イエロー
    private static final int COLOR_LABEL     = 0xFF6B6880; // 白パネル上のセクションラベル
    private static final int COLOR_CAPTION   = 0xFF8A87A0; // 白パネル上の補助キャプション
    private static final int COLOR_DIVIDER   = 0xFFDCDEEC; // 淡いラベンダーの区切り線
    private static final int COLOR_BACKDROP  = 0x88283050; // 背景ディム（真っ黒を避けた青みグレー）
    private static final int COLOR_TAG_GOLD  = 0xFFF8C442; // レッスン1回タグ（イエロー）

    private final List<ResourceLocation> availableCards = new ArrayList<>();
    private final List<ResourceLocation> deckCards = new ArrayList<>();

    private int scrollLeft = 0;
    private int scrollRight = 0;

    private int panelX, panelY;
    private int leftGridX, rightGridX, gridY, gridHeight;

    @Nullable private ResourceLocation hoveredCard = null;
    private int hoveredCardX;
    private int hoveredCardY;

    private record PillButton(int x, int y, int w, int h, String label, ResourceLocation texture, Runnable action) {
        boolean isHovered(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<PillButton> buttons = new ArrayList<>();

    public DeckEditorScreen() {
        super(Component.literal("デッキ編成"));
    }

    @Override
    protected void init() {
        super.init();
        // 習得済み（入手済み）カードのみ編成に使える
        availableCards.clear();
        availableCards.addAll(com.idolcraft.client.ClientDeckState.getOwnedCards());

        deckCards.clear();
        if (this.minecraft.player != null) {
            this.minecraft.player.getCapability(DeckCapability.DECK_DATA)
                    .ifPresent(deck -> deckCards.addAll(deck.getMasterCardList()));
        }

        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - PANEL_H) / 2;
        // 左グリッド（カード一覧）、右グリッド（デッキ）のX位置
        int gridAreaW = GRID_COLS * SLOT_SIZE * 2 + 24;
        this.leftGridX  = panelX + (PANEL_W - gridAreaW) / 2;
        this.rightGridX = leftGridX + GRID_COLS * SLOT_SIZE + 24;
        this.gridY = panelY + 92;
        this.gridHeight = GRID_ROWS * SLOT_SIZE;

        int btnY = panelY + PANEL_H - 40;
        int cx = panelX + PANEL_W / 2;
        int btnW = 84, btnH = 28, gap = 10;

        buttons.clear();
        buttons.add(new PillButton(cx - btnW - btnW / 2 - gap, btnY, btnW, btnH, "確定",
                TEX_PILL_CONFIRM, this::confirmDeck));
        buttons.add(new PillButton(cx - btnW / 2, btnY, btnW, btnH, "全削除",
                TEX_PILL_RESET, this::resetDeck));
        buttons.add(new PillButton(cx + btnW / 2 + gap, btnY, btnW, btnH, "キャンセル",
                TEX_PILL_CANCEL, this::onClose));
    }

    private void resetDeck() { deckCards.clear(); scrollRight = 0; }

    private void confirmDeck() {
        NetworkHandler.CHANNEL.sendToServer(new SetDeckPacket(new ArrayList<>(deckCards)));
        this.onClose();
    }

    // ========================================================================
    // 描画
    // ========================================================================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 背景暗転
        graphics.fill(0, 0, this.width, this.height, COLOR_BACKDROP);

        // ── パネル ──
        blitTex(graphics, TEX_PANEL, panelX, panelY, PANEL_W, PANEL_H, TEXSIZE_PANEL_W, TEXSIZE_PANEL_H);

        // ── ヘッダー ──
        int hx = panelX + (PANEL_W - HEADER_W) / 2;
        int hy = panelY - 18;
        blitTex(graphics, TEX_HEADER, hx, hy, HEADER_W, HEADER_H, TEXSIZE_HEADER_W, TEXSIZE_HEADER_H);
        graphics.drawCenteredString(this.font, "デッキ編成", panelX + PANEL_W / 2, hy + HEADER_H / 2 - 5, COLOR_TITLE);

        // ── セクションラベル ──
        graphics.drawString(this.font, "カード一覧 (" + availableCards.size() + ")",
                leftGridX, panelY + 70, COLOR_LABEL, false);
        String dLabel = "デッキ (" + deckCards.size() + "/" + MAX_DECK_SIZE + ")";
        int dw = this.font.width(dLabel);
        graphics.drawString(this.font, dLabel, rightGridX + GRID_COLS * SLOT_SIZE - dw, panelY + 70, COLOR_LABEL, false);

        // ── CMY装飾ディバイダ ──
        int divX = leftGridX + GRID_COLS * SLOT_SIZE + 12;
        graphics.fill(divX - 1, gridY - 6, divX, gridY + gridHeight + 6, COLOR_DIVIDER);
        // ディバイダの上下に小さなCMYの点（上=シアン, 下=ピンク）
        graphics.fill(divX - 2, gridY - 10, divX + 1, gridY - 6, COLOR_CYAN);
        graphics.fill(divX - 2, gridY + gridHeight + 6, divX + 1, gridY + gridHeight + 10, COLOR_PINK);

        // ── グリッド描画 ──
        hoveredCard = null;
        renderGrid(graphics, mouseX, mouseY, leftGridX,  availableCards, scrollLeft,  TEX_SLOT, false);
        renderGrid(graphics, mouseX, mouseY, rightGridX, deckCards,      scrollRight, TEX_SLOT_DECK, true);

        // ── 所持カードが無い場合の案内 ──
        if (availableCards.isEmpty()) {
            graphics.drawCenteredString(this.font, "カードを未所持です。パックを開封→カードを右クリックで習得しよう",
                    leftGridX + GRID_COLS * SLOT_SIZE / 2, gridY + gridHeight / 2 - 4, COLOR_CAPTION);
        }

        // ── キャプション ──
        graphics.drawString(this.font, "クリックで追加 →", leftGridX, gridY + gridHeight + 10, COLOR_CAPTION, false);
        String del = "← クリックで削除";
        graphics.drawString(this.font, del, rightGridX + GRID_COLS * SLOT_SIZE - this.font.width(del),
                gridY + gridHeight + 10, COLOR_CAPTION, false);

        // ── ボタン ──
        renderButtons(graphics, mouseX, mouseY);

        // ── ツールチップ（最前面） ──
        if (hoveredCard != null) {
            // 全画面をわずかに暗くしてツールチップの最前面感を強調（青みグレー）
            graphics.fill(0, 0, this.width, this.height, 0x88283050);
            // Depth test を切って確実に前面描画
            RenderSystem.disableDepthTest();
            CardRegistry.get(hoveredCard).ifPresent(def ->
                    CardTooltipRenderer.render(graphics, this.font, def, hoveredCardX, hoveredCardY,
                            this.width, this.height, panelX, panelY, PANEL_W, PANEL_H));
            RenderSystem.enableDepthTest();
        }
    }

    private void blitTex(GuiGraphics g, ResourceLocation tex, int x, int y, int dw, int dh, int tw, int th) {
        GuiTextures.bindSmooth(tex);
        g.blit(tex, x, y, dw, dh, 0f, 0f, tw, th, tw, th);
    }

    private static float breathe(float speed) {
        return 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 1000.0 * speed);
    }

    private void renderButtons(GuiGraphics g, int mx, int my) {
        float pulse = 1.0f + 0.03f * breathe(2.2f);
        for (int i = 0; i < buttons.size(); i++) {
            PillButton btn = buttons.get(i);
            boolean hov = btn.isHovered(mx, my);
            float scale = hov ? 1.08f : (i == 0 ? pulse : 1.0f);

            var pose = g.pose();
            pose.pushPose();
            float cx = btn.x() + btn.w() / 2f;
            float cy = btn.y() + btn.h() / 2f;
            pose.translate(cx, cy, 0);
            pose.scale(scale, scale, 1f);
            pose.translate(-cx, -cy, 0);

            float shade = hov ? 1.10f : 0.93f;
            RenderSystem.setShaderColor(shade, shade, shade, 1f);
            GuiTextures.bindSmooth(btn.texture());
            g.blit(btn.texture(), btn.x(), btn.y(), btn.w(), btn.h(),
                    0f, 0f, TEXSIZE_PILL_W, TEXSIZE_PILL_H, TEXSIZE_PILL_W, TEXSIZE_PILL_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int tw = this.font.width(btn.label());
            g.drawString(this.font, btn.label(),
                    btn.x() + btn.w() / 2 - tw / 2, btn.y() + btn.h() / 2 - 4, 0xFFFFFFFF, true);
            pose.popPose();
        }
    }

    private void renderGrid(GuiGraphics g, int mx, int my, int gx,
                            List<ResourceLocation> list, int scroll, ResourceLocation slotTex, boolean isDeckSide) {
        int first = scroll * GRID_COLS;
        int total = GRID_COLS * GRID_ROWS;
        for (int s = 0; s < total; s++) {
            int idx = first + s;
            int col = s % GRID_COLS;
            int row = s / GRID_COLS;
            int x = gx + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;
            int is = SLOT_SIZE - 2;

            boolean hov = mx >= x && mx < x + is && my >= y && my < y + is;

            var pose = g.pose();
            pose.pushPose();
            if (hov) {
                float scx = x + is / 2f;
                float scy = y + is / 2f;
                pose.translate(scx, scy, 0);
                pose.scale(1.14f, 1.14f, 1f);
                pose.translate(-scx, -scy, 0);
            }

            ResourceLocation bg = hov ? TEX_SLOT_HOVER : slotTex;
            GuiTextures.bindSmooth(bg);
            g.blit(bg, x, y, is, is, 0f, 0f, TEXSIZE_SLOT, TEXSIZE_SLOT, TEXSIZE_SLOT, TEXSIZE_SLOT);

            if (idx < list.size()) {
                ResourceLocation cardId = list.get(idx);
                Item item = ForgeRegistries.ITEMS.getValue(cardId);
                if (item != null) {
                    var ip = g.pose();
                    ip.pushPose();
                    ip.translate(x + is / 2f - 8, y + is / 2f - 8, 0);
                    g.renderItem(new ItemStack(item), 0, 0);
                    ip.popPose();

                    CardRegistry.get(cardId).ifPresent(def -> {
                        if (def.getType() == CardType.ONCE_PER_LESSON) {
                            g.fill(x + is - 7, y + 2, x + is - 1, y + 7, COLOR_TAG_GOLD);
                        }
                    });

                    if (hov) {
                        hoveredCard = cardId;
                        hoveredCardX = isDeckSide ? x : x + is; // 左グリッドは右側に、右グリッドは左側にツールチップ表示
                        hoveredCardY = y + is / 2;
                    }
                }
            }
            pose.popPose();
        }
    }

    // ========================================================================
    // 入力
    // ========================================================================

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            for (PillButton b : buttons) {
                if (b.isHovered(mx, my)) { b.action().run(); return true; }
            }
            ResourceLocation ca = pickFromGrid(mx, my, leftGridX,  availableCards, scrollLeft);
            if (ca != null) {
                if (deckCards.size() < MAX_DECK_SIZE) deckCards.add(ca);
                return true;
            }
            ResourceLocation cd = pickFromGrid(mx, my, rightGridX, deckCards, scrollRight);
            if (cd != null) { deckCards.remove(cd); return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Nullable
    private ResourceLocation pickFromGrid(double mx, double my, int gx, List<ResourceLocation> list, int scroll) {
        if (mx < gx || mx >= gx + GRID_COLS * SLOT_SIZE) return null;
        if (my < gridY || my >= gridY + gridHeight) return null;
        int col = (int) ((mx - gx) / SLOT_SIZE);
        int row = (int) ((my - gridY) / SLOT_SIZE);
        int idx = (scroll + row) * GRID_COLS + col;
        return (idx >= 0 && idx < list.size()) ? list.get(idx) : null;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (mx >= leftGridX && mx < leftGridX + GRID_COLS * SLOT_SIZE
                && my >= gridY && my < gridY + gridHeight) {
            scrollLeft = clamp(scrollLeft - (int) Math.signum(delta), availableCards.size());
            return true;
        }
        if (mx >= rightGridX && mx < rightGridX + GRID_COLS * SLOT_SIZE
                && my >= gridY && my < gridY + gridHeight) {
            scrollRight = clamp(scrollRight - (int) Math.signum(delta), deckCards.size());
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    private int clamp(int s, int count) {
        int rows = (int) Math.ceil(count / (double) GRID_COLS);
        return Math.max(0, Math.min(s, Math.max(0, rows - GRID_ROWS)));
    }

    @Override public void onClose() { this.minecraft.setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }
}

