package com.gakumas.produce.client.gui;

import com.gakumas.produce.card.CardRegistry;
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
 * 手帳所持中のみ表示されるHUD。
 * 左端：バフアイコン一覧（好調・絶好調は残り秒数をリアルタイム表示）。
 * 下部：手札3枚をカードスロット風テクスチャで表示。選択中のカードはゴールドハイライト＋少し上に。
 * プロデューサーランク（Pレベル）は常時表示のバナーとしては出さず、カードのツールチップでのみ確認できるようにしている。
 */
public class GakumasHudOverlay implements IGuiOverlay {

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_TEX = GuiTextures.SLOT_TEX;
    /** card_slot系テクスチャの余白（影のにじみ用マージン）が全体サイズに占める比率。gen_gui_textures.py: margin=48, 合計=312 */
    private static final float SLOT_MARGIN_RATIO = 48f / 312f;

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;

        renderBuffColumn(graphics, screenHeight);

        boolean holdingHandbook = mc.player.getMainHandItem().getItem() instanceof HandbookItem
                || mc.player.getOffhandItem().getItem() instanceof HandbookItem;
        if (!holdingHandbook) return;

        renderHand(mc, graphics, screenWidth, screenHeight);
    }

    private static final int BUFF_ICON_SIZE = 18;
    private static final int BUFF_LINE_W = 84;
    private static final int BUFF_LINE_H = 20;

    private void renderBuffColumn(GuiGraphics graphics, int screenHeight) {
        int x = 6;
        int y = screenHeight / 2 - 44;
        int lineSpacing = 22;

        int focus = ClientDeckState.getFocusStacks();
        int goodTicks = ClientDeckState.getGoodTicks();
        int greatTicks = ClientDeckState.getGreatTicks();

        if (focus > 0) {
            drawBuffLine(graphics, x, y, GuiTextures.ICON_FOCUS, "x" + focus);
            y += lineSpacing;
        }
        if (goodTicks > 0) {
            drawBuffLine(graphics, x, y, GuiTextures.ICON_GOOD_CONDITION, formatSeconds(goodTicks));
            y += lineSpacing;
        }
        if (greatTicks > 0) {
            drawBuffLine(graphics, x, y, GuiTextures.ICON_GREAT_CONDITION, formatSeconds(greatTicks));
            y += lineSpacing;
        }
    }

    private void drawBuffLine(GuiGraphics graphics, int x, int y, ResourceLocation icon, String value) {
        // 丸みのある半透明パネル風の背景（角を落とすため4隅を少しだけ削る簡易処理）
        graphics.fill(x + 1, y, x + BUFF_LINE_W - 1, y + BUFF_LINE_H, 0xAA1A1626);
        graphics.fill(x, y + 1, x + BUFF_LINE_W, y + BUFF_LINE_H - 1, 0xAA1A1626);

        int iconY = y + (BUFF_LINE_H - BUFF_ICON_SIZE) / 2;
        int tex = GuiTextures.BUFF_ICON_TEX;
        graphics.blit(icon, x + 3, iconY, BUFF_ICON_SIZE, BUFF_ICON_SIZE, 0f, 0f, tex, tex, tex, tex);

        graphics.drawString(Minecraft.getInstance().font, Component.literal(value),
                x + 3 + BUFF_ICON_SIZE + 6, y + BUFF_LINE_H / 2 - 4, 0xFFFFFFFF, true);
    }

    private String formatSeconds(int ticks) {
        double seconds = ticks / 20.0;
        return String.format(Locale.US, "%.1fs", seconds);
    }

    private void renderHand(Minecraft mc, GuiGraphics graphics, int screenWidth, int screenHeight) {
        List<ResourceLocation> hand = ClientDeckState.getHand();
        if (hand.isEmpty()) return;

        int spacing = 4;
        int totalWidth = hand.size() * SLOT_SIZE + (hand.size() - 1) * spacing;
        int startX = screenWidth / 2 - totalWidth / 2;
        int baseY = screenHeight - 76;

        int selected = ClientDeckState.getSelectedIndex();
        // 選択中カードだけ、ゆっくり呼吸するように拡縮させて「これが発動する」感を出す
        float pulse = 1.0f + 0.05f * (0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 1000.0 * 2.4));

        // GUI座標系でのマウス位置（IGuiOverlayはマウス座標を受け取らないため手動で変換する）
        double mouseX = mc.mouseHandler.xpos() * (double) screenWidth / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double) screenHeight / mc.getWindow().getScreenHeight();
        ResourceLocation hoveredCard = null;

        for (int i = 0; i < hand.size(); i++) {
            int x = startX + i * (SLOT_SIZE + spacing);
            int y = baseY - (i == selected ? 8 : 0); // 選択中のカードは少し上にハイライト

            var pose = graphics.pose();
            pose.pushPose();
            if (i == selected) {
                float cx = x + SLOT_SIZE / 2f;
                float cy = y + SLOT_SIZE / 2f;
                pose.translate(cx, cy, 0);
                pose.scale(pulse, pulse, 1f);
                pose.translate(-cx, -cy, 0);
            }

            ResourceLocation slotTex = (i == selected) ? GuiTextures.SLOT_HOVER : GuiTextures.SLOT;
            graphics.blit(slotTex, x, y, SLOT_SIZE, SLOT_SIZE, 0f, 0f, SLOT_TEX, SLOT_TEX, SLOT_TEX, SLOT_TEX);

            Item item = ForgeRegistries.ITEMS.getValue(hand.get(i));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                int iconX = x + (SLOT_SIZE - 16) / 2;
                int iconY = y + (SLOT_SIZE - 16) / 2;
                graphics.renderItem(stack, iconX, iconY);
                graphics.renderItemDecorations(mc.font, stack, iconX, iconY);
            }

            // 今使用できないカードは薄暗くする。テクスチャは影のにじみ分の余白を含むため、
            // 矩形オーバーレイをそのまま全面にかけると見た目の丸いカード枠からはみ出してズレて見える。
            // 余白比率（margin/合計サイズ）ぶんだけ内側に縮めて、実際に見えているカード枠に合わせる。
            if (!ClientDeckState.isHandUsable(i)) {
                int inset = Math.round(SLOT_SIZE * SLOT_MARGIN_RATIO);
                graphics.fill(x + inset, y + inset, x + SLOT_SIZE - inset, y + SLOT_SIZE - inset, 0xB0101018);
            }

            pose.popPose();

            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y - 8 && mouseY < y + SLOT_SIZE) {
                hoveredCard = hand.get(i);
            }
        }

        if (hoveredCard != null) {
            ResourceLocation cardId = hoveredCard;
            CardRegistry.get(cardId).ifPresent(def ->
                    CardTooltipRenderer.render(graphics, mc.font, def, (int) mouseX, (int) mouseY, screenWidth, screenHeight));
        }
    }
}
