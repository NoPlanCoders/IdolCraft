package com.gakumas.produce.client.gui;

import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * カード詳細ツールチップ v2 ─ 学マス風豪華版。
 * 大きめのパネルにカード名・効果・コストを整然と表示。
 * ホバー対象カードの横にスライドインするように表示し、
 * 周囲を暗くするオーバーレイで最前面感を強調。
 */
public final class CardTooltipRenderer {

    private CardTooltipRenderer() {}

    // ── カラー ──
    private static final int BG_DIM       = 0x80101018;
    private static final int PANEL_FILL   = 0xF0F0F0FF;
    private static final int PANEL_BORDER = 0xFFA088C0;
    private static final int PANEL_GLOW   = 0x30C0A8E0;
    private static final int TITLE_COLOR  = 0xFF2A1E40;
    private static final int TAG_GOLD     = 0xFFC49A35;
    private static final int COST_RED     = 0xFFC06050;
    private static final int DESC_BLUE    = 0xFF5A82A8;
    private static final int PLEVEL_PURPLE = 0xFF8570C0;
    private static final int GOLD_LINE    = 0xFFD4A843;

    private record Line(String text, int color, int iconColor) {}
    private static Line line(String text, int color) { return new Line(text, color, 0); }
    private static Line icon(String text, int color, int iconColor) { return new Line(text, color, iconColor); }

    /**
     * @param anchorX  ホバー中カードの端X座標
     * @param anchorY  ホバー中カードの中心Y座標
     * @param screenW/H 画面サイズ
     * @param panelX/Y/W/H デッキ編集パネルの領域（ツールチップがパネル外に出ないようにする）
     */
    public static void render(GuiGraphics g, Font font, CardDefinition def,
                               int anchorX, int anchorY,
                               int screenW, int screenH,
                               int panelX, int panelY, int panelW, int panelH) {
        List<Line> lines = new ArrayList<>();
        lines.add(line(def.getDisplayName(), TITLE_COLOR));
        if (def.getType() == CardType.ONCE_PER_LESSON) {
            lines.add(line("◆ レッスン中1回", TAG_GOLD));
        }
        if (def.getHpCost() > 0) {
            lines.add(icon("消費体力 " + def.getHpCost(), COST_RED, COST_RED));
        }
        if (!def.getDescription().isEmpty()) {
            // 長い説明文は改行可能にする簡易対応（16文字程度で折り返し）
            String desc = def.getDescription();
            if (desc.length() > 16) {
                // 全角半角混在を考慮して単純に分割
                int split = desc.length() / 2;
                // なるべくスペースや読点の近くで分割
                int best = split;
                for (int i = Math.max(0, split - 3); i < Math.min(desc.length(), split + 4); i++) {
                    char c = desc.charAt(i);
                    if (c == ' ' || c == '　' || c == '、' || c == '，') { best = i + 1; break; }
                }
                lines.add(icon(desc.substring(0, best).trim(), DESC_BLUE, 0xFF6FB6E8));
                lines.add(icon(desc.substring(best).trim(), DESC_BLUE, 0xFF6FB6E8));
            } else {
                lines.add(icon(desc, DESC_BLUE, 0xFF6FB6E8));
            }
        }
        if (!def.getUsabilityHint().isEmpty()) {
            lines.add(icon("使用条件 " + def.getUsabilityHint(), DESC_BLUE, 0xFFE0C468));
        }
        if (def.getRequiredPLevel() > 0) {
            lines.add(icon("必要Pレベル " + def.getRequiredPLevel(), PLEVEL_PURPLE, 0xFFB79AE0));
        }

        int pad = 10;
        int lineH = 13;
        int gap = 2;
        int maxW = 0;
        for (Line l : lines) {
            int w = font.width(l.text) + (l.iconColor != 0 ? 10 : 0);
            maxW = Math.max(maxW, w);
        }
        int contentW = Math.max(130, maxW + pad * 2);
        int bodyH = lines.size() * lineH + (lines.size() - 1) * gap + pad * 2 + 12;

        // anchorXの方向に応じて左右どちらに出すか決定
        boolean showRight = anchorX < panelX + panelW / 2;
        int x = showRight ? anchorX + 10 : anchorX - contentW - 10;
        int y = anchorY - bodyH / 2;

        // 画面端・パネル端でクランプ
        x = Math.max(panelX + 4, Math.min(x, panelX + panelW - contentW - 4));
        y = Math.max(panelY + 4, Math.min(y, panelY + panelH - bodyH - 4));

        // ── 背面暗転（ツールチップ周辺だけ） ──
        g.fill(x - 6, y - 6, x + contentW + 6, y + bodyH + 6, BG_DIM);

        // ── パネル本体 ──
        // 白地
        g.fill(x, y, x + contentW, y + bodyH, PANEL_FILL);
        // 紫縁
        g.fill(x, y, x + contentW, y + 1, PANEL_BORDER);
        g.fill(x, y + bodyH - 1, x + contentW, y + bodyH, PANEL_BORDER);
        g.fill(x, y, x + 1, y + bodyH, PANEL_BORDER);
        g.fill(x + contentW - 1, y, x + contentW, y + bodyH, PANEL_BORDER);
        // グロー
        g.fill(x - 2, y - 2, x + contentW + 2, y, PANEL_GLOW);
        g.fill(x - 2, y + bodyH, x + contentW + 2, y + bodyH + 2, PANEL_GLOW);

        // ── ゴールド装飾線（上部） ──
        g.fill(x + 8, y + 4, x + contentW - 8, y + 6, GOLD_LINE);

        // ── テキスト描画 ──
        int ty = y + pad + 6;
        for (Line l : lines) {
            int tx = x + pad;
            if (l.iconColor != 0) {
                g.fill(tx, ty + 2, tx + 6, ty + 9, l.iconColor);
                tx += 10;
            }
            g.drawString(font, l.text, tx, ty, l.color, false);
            ty += lineH + gap;
        }
    }
}
