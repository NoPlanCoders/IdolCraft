package com.gakumas.produce.client.gui;

import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.card.CardType;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.network.NetworkHandler;
import com.gakumas.produce.network.packet.SetDeckPacket;
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
 * デッキ編成画面。
 * 学マス風の淡いラベンダー×ゴールドのカードゲームUIを、専用GUIテクスチャ
 * （assets/gakumas_produce/textures/gui/）で再現している。
 * 左側に登録済み全カード、右側に現在のデッキ構成をアイテムアイコン付きグリッドで表示する。
 * 左側のカードをクリックするとデッキに1枚追加、右側のカードをクリックするとデッキから1枚削除。
 */
public class DeckEditorScreen extends Screen {

    private static final ResourceLocation TEX_PANEL = GuiTextures.PANEL;
    private static final ResourceLocation TEX_HEADER = GuiTextures.HEADER;
    private static final ResourceLocation TEX_SLOT = GuiTextures.SLOT;
    private static final ResourceLocation TEX_SLOT_DECK = GuiTextures.SLOT_DECK;
    private static final ResourceLocation TEX_SLOT_HOVER = GuiTextures.SLOT_HOVER;
    private static final ResourceLocation TEX_PILL_CONFIRM = GuiTextures.PILL_CONFIRM;
    private static final ResourceLocation TEX_PILL_RESET = GuiTextures.PILL_RESET;
    private static final ResourceLocation TEX_PILL_CANCEL = GuiTextures.PILL_CANCEL;

    // 実ファイルのピクセルサイズ（gen_gui_textures.py で SCALE=3 のもとに生成）
    private static final int TEXSIZE_PANEL_W = GuiTextures.PANEL_TEX_W;
    private static final int TEXSIZE_PANEL_H = GuiTextures.PANEL_TEX_H;
    private static final int TEXSIZE_HEADER_W = GuiTextures.HEADER_TEX_W;
    private static final int TEXSIZE_HEADER_H = GuiTextures.HEADER_TEX_H;
    private static final int TEXSIZE_SLOT = GuiTextures.SLOT_TEX;
    private static final int TEXSIZE_PILL_W = GuiTextures.PILL_TEX_W;
    private static final int TEXSIZE_PILL_H = GuiTextures.PILL_TEX_H;

    private static final int PANEL_W = 400;
    private static final int PANEL_H = 300;
    private static final int HEADER_W = 260;
    private static final int HEADER_H = 70;
    private static final int SLOT_SIZE = 34;
    private static final int GRID_COLS = 5;
    private static final int GRID_ROWS = 4;
    private static final int MAX_DECK_SIZE = 30;

    // ── 学マス再解釈カラーパレット ──
    private static final int GOLD_TEXT = 0xFFD4A843;
    private static final int LABEL_TEXT = 0xFF9A93B0;
    private static final int CAPTION_TEXT = 0xFFB0A8C8;

    private final List<ResourceLocation> availableCards = new ArrayList<>();
    private final List<ResourceLocation> deckCards = new ArrayList<>();

    private int scrollLeft = 0;
    private int scrollRight = 0;

    private int panelX;
    private int panelY;
    private int leftGridX;
    private int rightGridX;
    private int gridY;
    private int gridHeight;

    @Nullable private ResourceLocation hoveredCard = null;
    private int hoveredMouseX;
    private int hoveredMouseY;

    /** 見た目だけの独自ピルボタン（バニラButtonの灰色スキンを使わず自前描画するための構造体） */
    private record PillButton(int x, int y, int w, int h, String label, ResourceLocation texture, Runnable action) {
        boolean isHovered(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private final List<PillButton> buttons = new ArrayList<>();

    public DeckEditorScreen() {
        super(Component.literal("デッキ編成"));
    }

    @Override
    protected void init() {
        super.init();

        availableCards.clear();
        for (CardDefinition card : CardRegistry.all()) {
            availableCards.add(card.getId());
        }

        deckCards.clear();
        if (this.minecraft.player != null) {
            this.minecraft.player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck ->
                    deckCards.addAll(deck.getMasterCardList()));
        }

        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - PANEL_H) / 2;
        this.leftGridX = panelX + 22;
        this.rightGridX = panelX + PANEL_W - 22 - GRID_COLS * SLOT_SIZE;
        this.gridY = panelY + 86;
        this.gridHeight = GRID_ROWS * SLOT_SIZE;

        int buttonY = panelY + PANEL_H - 38;
        int centerX = panelX + PANEL_W / 2;
        int btnW = 78, btnH = 26, gap = 8;

        buttons.clear();
        buttons.add(new PillButton(centerX - btnW - btnW / 2 - gap, buttonY, btnW, btnH, "確定",
                TEX_PILL_CONFIRM, this::confirmDeck));
        buttons.add(new PillButton(centerX - btnW / 2, buttonY, btnW, btnH, "全削除",
                TEX_PILL_RESET, this::resetDeck));
        buttons.add(new PillButton(centerX + btnW / 2 + gap, buttonY, btnW, btnH, "キャンセル",
                TEX_PILL_CANCEL, this::onClose));
    }

    private void resetDeck() {
        deckCards.clear();
        scrollRight = 0;
    }

    private void confirmDeck() {
        NetworkHandler.CHANNEL.sendToServer(new SetDeckPacket(new ArrayList<>(deckCards)));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101018);

        blitTexture(graphics, TEX_PANEL, panelX, panelY, PANEL_W, PANEL_H, TEXSIZE_PANEL_W, TEXSIZE_PANEL_H);

        int headerX = panelX + (PANEL_W - HEADER_W) / 2;
        int headerY = panelY - 16;
        blitTexture(graphics, TEX_HEADER, headerX, headerY, HEADER_W, HEADER_H, TEXSIZE_HEADER_W, TEXSIZE_HEADER_H);
        graphics.drawCenteredString(this.font, "デッキ編成", panelX + PANEL_W / 2, headerY + HEADER_H / 2 - 4, GOLD_TEXT);

        graphics.drawString(this.font, "カード一覧 (" + availableCards.size() + ")", leftGridX, panelY + 66, LABEL_TEXT, false);
        String deckLabel = "デッキ (" + deckCards.size() + "/" + MAX_DECK_SIZE + ")";
        int deckLabelWidth = this.font.width(deckLabel);
        graphics.drawString(this.font, deckLabel, rightGridX + GRID_COLS * SLOT_SIZE - deckLabelWidth, panelY + 66, LABEL_TEXT, false);

        int dividerX = panelX + PANEL_W / 2;
        graphics.fill(dividerX, gridY - 6, dividerX + 1, gridY + gridHeight + 6, 0x30574F8A);

        hoveredCard = null;
        hoveredMouseX = mouseX;
        hoveredMouseY = mouseY;

        renderGrid(graphics, mouseX, mouseY, leftGridX, availableCards, scrollLeft, TEX_SLOT);
        renderGrid(graphics, mouseX, mouseY, rightGridX, deckCards, scrollRight, TEX_SLOT_DECK);

        graphics.drawString(this.font, "クリックで追加 →", leftGridX, gridY + gridHeight + 8, CAPTION_TEXT, false);
        String delCaption = "← クリックで削除";
        int delCaptionWidth = this.font.width(delCaption);
        graphics.drawString(this.font, delCaption, rightGridX + GRID_COLS * SLOT_SIZE - delCaptionWidth, gridY + gridHeight + 8, CAPTION_TEXT, false);

        renderButtons(graphics, mouseX, mouseY);

        if (hoveredCard != null) {
            renderCardTooltip(graphics, hoveredCard, hoveredMouseX, hoveredMouseY);
        }
    }

    /** テクスチャ全体を (drawW x drawH) に拡縮して描画する。texW/texH は実ファイルのピクセルサイズ。 */
    private void blitTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int drawW, int drawH, int texW, int texH) {
        GuiTextures.bindSmooth(texture); // 角丸の縁を滑らかに（ニアレストネイバーによるジャギー対策）
        graphics.blit(texture, x, y, drawW, drawH, 0f, 0f, texW, texH, texW, texH);
    }

    /** 呼吸するようにゆっくり明滅する 0.0〜1.0 の値（時間ベース、フレームレートに依存しない） */
    private static float breathe(float speed) {
        return 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 1000.0 * speed);
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        // 「確定」ボタンだけ常にゆっくり呼吸するように拡縮させ、押したくなる主役感を出す
        float pulse = 1.0f + 0.035f * breathe(2.2f);

        for (int i = 0; i < buttons.size(); i++) {
            PillButton btn = buttons.get(i);
            boolean hovered = btn.isHovered(mouseX, mouseY);
            float scale = hovered ? 1.08f : (i == 0 ? pulse : 1.0f);

            var pose = graphics.pose();
            pose.pushPose();
            float cx = btn.x() + btn.w() / 2f;
            float cy = btn.y() + btn.h() / 2f;
            pose.translate(cx, cy, 0);
            pose.scale(scale, scale, 1f);
            pose.translate(-cx, -cy, 0);

            float shade = hovered ? 1.08f : 0.94f;
            RenderSystem.setShaderColor(shade, shade, shade, 1f);
            GuiTextures.bindSmooth(btn.texture());
            graphics.blit(btn.texture(), btn.x(), btn.y(), btn.w(), btn.h(), 0f, 0f, TEXSIZE_PILL_W, TEXSIZE_PILL_H, TEXSIZE_PILL_W, TEXSIZE_PILL_H);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int textWidth = this.font.width(btn.label());
            graphics.drawString(this.font, btn.label(),
                    btn.x() + btn.w() / 2 - textWidth / 2, btn.y() + btn.h() / 2 - 4,
                    0xFFFFFFFF, true);

            pose.popPose();
        }
    }

    /** カードIDのリストをアイコン付きグリッドとして描画し、ホバー中のカードを検出する */
    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY, int gridX, List<ResourceLocation> list, int scrollRows, ResourceLocation slotTexture) {
        int firstIndex = scrollRows * GRID_COLS;
        int visibleSlots = GRID_COLS * GRID_ROWS;

        for (int slot = 0; slot < visibleSlots; slot++) {
            int index = firstIndex + slot;
            int col = slot % GRID_COLS;
            int row = slot / GRID_COLS;
            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE;
            int innerSize = SLOT_SIZE - 2;

            boolean hovering = mouseX >= x && mouseX < x + innerSize && mouseY >= y && mouseY < y + innerSize;

            var pose = graphics.pose();
            pose.pushPose();
            if (hovering) {
                // ホバー時にスロットごとポップアップ拡大させ、選べる手応え（動き）を出す
                float cx = x + innerSize / 2f;
                float cy = y + innerSize / 2f;
                pose.translate(cx, cy, 0);
                pose.scale(1.14f, 1.14f, 1f);
                pose.translate(-cx, -cy, 0);
            }

            ResourceLocation bg = hovering ? TEX_SLOT_HOVER : slotTexture;
            GuiTextures.bindSmooth(bg);
            graphics.blit(bg, x, y, innerSize, innerSize, 0f, 0f, TEXSIZE_SLOT, TEXSIZE_SLOT, TEXSIZE_SLOT, TEXSIZE_SLOT);

            if (index < list.size()) {
                ResourceLocation cardId = list.get(index);
                Item item = ForgeRegistries.ITEMS.getValue(cardId);
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    var itemPose = graphics.pose();
                    itemPose.pushPose();
                    itemPose.translate(x + innerSize / 2f - 8, y + innerSize / 2f - 8, 0);
                    graphics.renderItem(stack, 0, 0);
                    itemPose.popPose();

                    // 本家同様「色分けで重要度を一目で分かるように」：レッスン中1回カードには金色マーカーを付ける
                    CardRegistry.get(cardId).ifPresent(def -> {
                        if (def.getType() == CardType.ONCE_PER_LESSON) {
                            int mx = x + innerSize - 6;
                            int my = y + 2;
                            graphics.fill(mx, my, mx + 5, my + 5, 0xFFE8C97A);
                        }
                    });

                    if (hovering) {
                        hoveredCard = cardId;
                    }
                }
            }

            pose.popPose();
        }
    }

    private void renderCardTooltip(GuiGraphics graphics, ResourceLocation cardId, int mouseX, int mouseY) {
        CardRegistry.get(cardId).ifPresent(def ->
                CardTooltipRenderer.render(graphics, this.font, def, mouseX, mouseY, this.width, this.height));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (PillButton btn : buttons) {
                if (btn.isHovered(mouseX, mouseY)) {
                    btn.action().run();
                    return true;
                }
            }

            ResourceLocation clickedAvailable = pickFromGrid(mouseX, mouseY, leftGridX, availableCards, scrollLeft);
            if (clickedAvailable != null) {
                if (deckCards.size() < MAX_DECK_SIZE) {
                    deckCards.add(clickedAvailable);
                }
                return true;
            }

            ResourceLocation clickedDeck = pickFromGrid(mouseX, mouseY, rightGridX, deckCards, scrollRight);
            if (clickedDeck != null) {
                deckCards.remove(clickedDeck);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    private ResourceLocation pickFromGrid(double mouseX, double mouseY, int gridX, List<ResourceLocation> list, int scrollRows) {
        if (mouseX < gridX || mouseX >= gridX + GRID_COLS * SLOT_SIZE) return null;
        if (mouseY < gridY || mouseY >= gridY + gridHeight) return null;

        int col = (int) ((mouseX - gridX) / SLOT_SIZE);
        int row = (int) ((mouseY - gridY) / SLOT_SIZE);
        int index = (scrollRows + row) * GRID_COLS + col;

        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= leftGridX && mouseX < leftGridX + GRID_COLS * SLOT_SIZE
                && mouseY >= gridY && mouseY < gridY + gridHeight) {
            scrollLeft = clampScroll(scrollLeft - (int) Math.signum(delta), availableCards.size());
            return true;
        }
        if (mouseX >= rightGridX && mouseX < rightGridX + GRID_COLS * SLOT_SIZE
                && mouseY >= gridY && mouseY < gridY + gridHeight) {
            scrollRight = clampScroll(scrollRight - (int) Math.signum(delta), deckCards.size());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int clampScroll(int scroll, int itemCount) {
        int totalRows = (int) Math.ceil(itemCount / (double) GRID_COLS);
        int maxScroll = Math.max(0, totalRows - GRID_ROWS);
        return Math.max(0, Math.min(scroll, maxScroll));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
