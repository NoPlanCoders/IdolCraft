package com.gakumas.produce.client.gui;

import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * カード詳細ポップアップの共通描画。
 * 本家学マスの「カード詳細（丸みのある白パネル＋アイコン付き効果説明、下端にホバー対象を指す
 * 吹き出しのしっぽ）」を参考にした見た目で、デッキ編成画面・手札HUDの両方から呼び出せるようにしてある。
 */
public final class CardTooltipRenderer {

    private CardTooltipRenderer() {}

    private static final int TITLE_COLOR = 0xFF3A3548;
    private static final int GOLD_TAG_COLOR = 0xFFAF7F1E;
    private static final int COST_COLOR = 0xFFB0473F;
    private static final int DESC_COLOR = 0xFF4F6E8C;
    private static final int PLEVEL_COLOR = 0xFF7A5AAF;

    private record Line(String text, int color, int iconColor) {}

    /** アイコンなしの行 */
    private static Line line(String text, int color) {
        return new Line(text, color, 0);
    }

    /** 先頭に小さな色付きの丸アイコンが付く行 */
    private static Line iconLine(String text, int color, int iconColor) {
        return new Line(text, color, iconColor);
    }

    /**
     * @param anchorX 吹き出しのしっぽが指す先（ホバー対象カードの中心付近）
     * @param anchorY 吹き出しのしっぽが指す先
     */
    public static void render(GuiGraphics graphics, Font font, CardDefinition def, int anchorX, int anchorY, int screenWidth, int screenHeight) {
        List<Line> lines = new ArrayList<>();
        lines.add(line(def.getDisplayName(), TITLE_COLOR));
        if (def.getType() == CardType.ONCE_PER_LESSON) {
            lines.add(line("レッスン中1回", GOLD_TAG_COLOR));
        }
        if (def.getHpCost() > 0) {
            lines.add(iconLine("消費体力 " + def.getHpCost(), COST_COLOR, 0xFFE0554A));
        }
        if (!def.getDescription().isEmpty()) {
            lines.add(iconLine(def.getDescription(), DESC_COLOR, 0xFF6FB6E8));
        }
        if (!def.getUsabilityHint().isEmpty()) {
            lines.add(iconLine("使用条件 " + def.getUsabilityHint(), DESC_COLOR, 0xFFE0C468));
        }
        if (def.getRequiredPLevel() > 0) {
            lines.add(iconLine("必要Pレベル " + def.getRequiredPLevel(), PLEVEL_COLOR, 0xFFB79AE0));
        }

        int padding = 8;
        int lineHeight = 12;
        int maxTextWidth = 0;
        for (Line l : lines) {
            int w = font.width(l.text) + (l.iconColor != 0 ? 10 : 0);
            maxTextWidth = Math.max(maxTextWidth, w);
        }
        int contentW = Math.max(90, maxTextWidth + padding * 2);
        int bodyH = lines.size() * lineHeight + padding * 2 - 2;
        int totalH = Math.round(bodyH / GuiTextures.TOOLTIP_BODY_RATIO);

        // しっぽの先端が anchor（ホバー対象の中心）を指すように、本体を anchor の真上に中央揃えで配置する
        int x = anchorX - contentW / 2;
        int y = anchorY - totalH - 4;
        x = Math.max(2, Math.min(x, screenWidth - contentW - 2));
        boolean flipBelow = y < 2;
        if (flipBelow) {
            // 画面上端に収まらない場合は下に表示する（この場合しっぽは見た目上は上を指さないが、
            // パネル自体は視認できる位置を優先する）
            y = anchorY + 12;
        }

        GuiTextures.bindSmooth(GuiTextures.TOOLTIP_PANEL);
        graphics.blit(GuiTextures.TOOLTIP_PANEL, x, y, contentW, totalH,
                0f, 0f, GuiTextures.TOOLTIP_PANEL_TEX_W, GuiTextures.TOOLTIP_PANEL_TEX_H,
                GuiTextures.TOOLTIP_PANEL_TEX_W, GuiTextures.TOOLTIP_PANEL_TEX_H);

        // テクスチャの本体（角丸矩形）は常に上側にあるため、しっぽの有無に関わらずテキストは常にyから開始する
        int ty = y + padding;
        for (Line l : lines) {
            int tx = x + padding;
            if (l.iconColor != 0) {
                graphics.fill(tx, ty + 2, tx + 6, ty + 8, l.iconColor);
                tx += 10;
            }
            graphics.drawString(font, l.text, tx, ty, l.color, false);
            ty += lineHeight;
        }
    }
}
