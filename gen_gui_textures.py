#!/usr/bin/env python3
"""
学マス風 GUI テクスチャ生成スクリプト (再解釈版)
Minecraft Forge 1.20.1 向け gakumas_produce MOD 用。

デザイン方針:
  - 本家学マスの「深い紫〜ラベンダーのグラデーション × ゴールドのアクセント」をモチーフに
  - 角丸パネル + ソフトな影 + 発光ホバー
  - 原寸の数倍 (SCALE=3) で生成し、縮小表示時の品質を確保
"""
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

# ---------------------------------------------------------------------------
# カラーパレット ─ 学マス再解釈
# ---------------------------------------------------------------------------
C_DEEP_PURPLE  = (28, 18, 58)     # #1C123A  最深部
C_MID_PURPLE   = (45, 27, 105)    # #2D1B69  メインパネル背景
C_PANEL_FILL   = (245, 242, 255)  # #F5F2FF  白パネル地
C_PANEL_BORDER = (145, 120, 195)  # #9178C3  パネル縁
C_GOLD         = (212, 168, 67)   # #D4A843  ゴールド
C_GOLD_BRIGHT  = (240, 200, 100)  # #F0C864  発光ゴールド
C_GOLD_DIM     = (160, 120, 50)   # #A07832  控えめゴールド
C_HEADER_BG    = (38, 22, 78)     # #26164E  ヘッダー背景
C_HEADER_LINE  = (200, 170, 90)   # #C8AA5A  ヘッダー装飾線
C_SLOT_BG      = (28, 18, 55)     # #1C1237  スロット背景
C_SLOT_BORDER  = (130, 105, 175)  # #8269AF  スロット縁
C_SLOT_DECK_BG = (22, 15, 48)     # #160F30  デッキ側スロット背景
C_SHADOW       = (10, 5, 30)      # 影色
C_BTN_CREAM    = (232, 228, 248)  # #E8E4F8  ボタン地色（ニュートラル、シェーダー色乗算用）
C_ICON_FOCUS   = (255, 200, 100)  # 集中アイコン
C_ICON_GOOD    = (255, 140, 80)   # 好調アイコン
C_ICON_GREAT   = (255, 80, 120)   # 絶好調アイコン
C_TOOLTIP_BG   = (252, 250, 255)  # ツールチップ地
C_TOOLTIP_BD   = (170, 155, 210)  # ツールチップ縁
C_TEXT_DARK    = (58, 53, 72)     # #3A3548

# ---------------------------------------------------------------------------
# テクスチャ出力サイズ (現在のコードと一致)
# ---------------------------------------------------------------------------
SCALE  = 3
MARGIN = 48  # スロット系の影にじみ用マージン (3x16px)

TEX = {
    "panel":      (720 * SCALE, 540 * SCALE),   # 2160 x 1620
    "header":     (460 * SCALE, 124 * SCALE),   # 1380 x 372
    "slot":       (104 * SCALE, 104 * SCALE),   # 312 x 312
    "pill":       (192 * SCALE, 88 * SCALE),    # 576 x 264
    "buff_icon":  (32 * SCALE, 32 * SCALE),     # 96 x 96
    "tooltip":    (240 * SCALE, 174 * SCALE),   # 720 x 522
}


# ---------------------------------------------------------------------------
# 描画ユーティリティ
# ---------------------------------------------------------------------------

def rounded_rect_mask(size, radius):
    """角丸矩形のマスク (Image.new の mask に使える L モード)"""
    w, h = size
    mask = Image.new("L", (w, h), 0)
    d = ImageDraw.Draw(mask)
    d.rounded_rectangle((0, 0, w - 1, h - 1), radius=radius, fill=255)
    return mask


def draw_rounded_rect(draw, xy, radius, fill=None, outline=None, width=1):
    """Pillow の rounded_rectangle をラップ (outline があるときは塗り潰し＋外枠)"""
    if fill is not None:
        draw.rounded_rectangle(xy, radius=radius, fill=fill)
    if outline is not None and width > 0:
        draw.rounded_rectangle(xy, radius=radius, outline=outline, width=width)


def shadow(base_img, offset=(8, 10), blur=14, opacity=90):
    """画像の下に影を追加した新規画像を返す"""
    ow = abs(offset[0]) * 2 + blur * 2
    oh = abs(offset[1]) * 2 + blur * 2
    w, h = base_img.size
    canvas = Image.new("RGBA", (w + ow, h + oh), (0, 0, 0, 0))
    # 影レイヤー
    shadow_img = Image.new("RGBA", base_img.size, (*C_SHADOW, opacity))
    sx = ow // 2 - offset[0]
    sy = oh // 2 - offset[1]
    canvas.paste(shadow_img, (sx, sy), base_img.split()[-1])
    canvas = canvas.filter(ImageFilter.GaussianBlur(blur))
    # 本体を上に重ねる
    canvas.paste(base_img, (ow // 2, oh // 2), base_img)
    return canvas


def gradient_vertical(w, h, top, bottom):
    """縦グラデーションの Image"""
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        t = y / (h - 1) if h > 1 else 0
        r = int(top[0] + (bottom[0] - top[0]) * t)
        g = int(top[1] + (bottom[1] - top[1]) * t)
        b = int(top[2] + (bottom[2] - top[2]) * t)
        for x in range(w):
            img.putpixel((x, y), (r, g, b, 255))
    return img


# ---------------------------------------------------------------------------
# 各パーツ生成
# ---------------------------------------------------------------------------

def gen_panel():
    """デッキ編成画面の背景パネル"""
    w, h = TEX["panel"]  # 2160 x 1620
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # ── パネル本体: 白地 + 紫味の縁 + グラデーション背景 ──
    margin = 30
    r = 48  # 角丸半径

    # 影 (大きめに描いてぼかす)
    shadow_layer = Image.new("RGBA", (w + 60, h + 60), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow_layer)
    sd.rounded_rectangle((30, 30, w + 29, h + 29), radius=r + 10, fill=(*C_SHADOW, 80))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(18))
    img.paste(shadow_layer, (-30, -30), shadow_layer)

    # 本体背景 → 紫グラデーションから白へ
    bg = gradient_vertical(w - margin * 2, h - margin * 2,
                           C_MID_PURPLE, (60, 40, 110))
    img.paste(bg, (margin, margin), rounded_rect_mask(bg.size, r))

    # 内側に白いカード領域風のパネル。中央に大きく配置
    inner_margin = 60
    inner_w = w - inner_margin * 2
    inner_h = h - inner_margin * 2 - 20
    inner = Image.new("RGBA", (inner_w, inner_h), (0, 0, 0, 0))
    idraw = ImageDraw.Draw(inner)
    inner_r = 36
    # 内パネル影
    ishadow = Image.new("RGBA", (inner_w + 40, inner_h + 40), (0, 0, 0, 0))
    isd = ImageDraw.Draw(ishadow)
    isd.rounded_rectangle((20, 20, inner_w + 19, inner_h + 19), radius=inner_r, fill=(*C_SHADOW, 35))
    ishadow = ishadow.filter(ImageFilter.GaussianBlur(8))
    inner.paste(ishadow, (-20, -20), ishadow)
    # 白地
    idraw.rounded_rectangle((0, 0, inner_w - 1, inner_h - 1), radius=inner_r,
                            fill=C_PANEL_FILL, outline=C_PANEL_BORDER, width=3)

    # 白パネルの上端に細いゴールドライン
    idraw.rounded_rectangle((8, 8, inner_w - 9, 11), radius=2, fill=C_GOLD)

    img.paste(inner, (inner_margin, inner_margin + 10), inner)

    # 四隅にゴールドの小さなアクセント点
    for cx, cy in [(margin + 40, margin + 40),
                   (w - margin - 40, margin + 40),
                   (margin + 40, h - margin - 40),
                   (w - margin - 40, h - margin - 40)]:
        d.ellipse((cx - 6, cy - 6, cx + 6, cy + 6), fill=C_GOLD_BRIGHT)

    return img


def gen_header():
    """タイトルヘッダー"""
    w, h = TEX["header"]  # 1380 x 372
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 36

    # 影
    sh = Image.new("RGBA", (w + 40, h + 40), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle((20, 20, w + 19, h + 19), radius=r, fill=(*C_SHADOW, 90))
    sh = sh.filter(ImageFilter.GaussianBlur(12))
    img.paste(sh, (-20, -20), sh)

    # 本体: 紫グラデ
    bg = gradient_vertical(w, h, C_HEADER_BG, C_MID_PURPLE)
    mask = rounded_rect_mask((w, h), r)
    img.paste(bg, (0, 0), mask)

    # ゴールドの装飾ライン (上下)
    d.rounded_rectangle((40, 20, w - 40, 28), radius=4, fill=C_HEADER_LINE)
    d.rounded_rectangle((40, h - 28, w - 40, h - 20), radius=4, fill=C_HEADER_LINE)

    # 中央に細いゴールドのハイライト線
    d.rounded_rectangle((w // 2 - 120, 36, w // 2 + 120, 40), radius=2, fill=C_GOLD_BRIGHT)

    return img


def gen_slot(base_color=C_SLOT_BG, border_color=C_SLOT_BORDER, glow_color=None):
    """カードスロット枠"""
    w, h = TEX["slot"]  # 312 x 312
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    inner = int(w * (1 - 48 / 312 * 2))  # マージンを除いた内側サイズ
    inner = inner - inner % 2  # 偶数に
    offset = (w - inner) // 2
    r = 20

    # 影
    sh = Image.new("RGBA", (inner + 30, inner + 30), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle((15, 15, inner + 14, inner + 14), radius=r, fill=(*C_SHADOW, 100))
    sh = sh.filter(ImageFilter.GaussianBlur(8))
    img.paste(sh, (offset - 15, offset - 15), sh)

    # ベース
    d.rounded_rectangle((offset, offset, offset + inner - 1, offset + inner - 1),
                        radius=r, fill=base_color, outline=border_color, width=3)

    # 内側にゴールドの細枠
    inset = 6
    d.rounded_rectangle((offset + inset, offset + inset,
                         offset + inner - inset - 1, offset + inner - inset - 1),
                        radius=r - inset, outline=(*border_color, 100), width=1)

    # グロー効果（ホバー時用）
    if glow_color:
        glow = Image.new("RGBA", (inner + 40, inner + 40), (0, 0, 0, 0))
        gd = ImageDraw.Draw(glow)
        gd.rounded_rectangle((20, 20, inner + 19, inner + 19), radius=r + 4,
                             outline=glow_color, width=6)
        glow = glow.filter(ImageFilter.GaussianBlur(6))
        img.paste(glow, (offset - 20, offset - 20), glow)

    return img


def gen_pill(color):
    """ピル型ボタン。上部ハイライト＋下部陰影で立体感を出す"""
    w, h = TEX["pill"]  # 576 x 264
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = h // 2 - 4

    mx = 4  # 影・縁用の外側マージン
    iw, ih = w - mx * 2, h - mx * 2
    ir = ih // 2 - 2

    # 影
    sh = Image.new("RGBA", (iw + 24, ih + 24), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle((12, 12, iw + 11, ih + 11), radius=ir, fill=(*C_SHADOW, 90))
    sh = sh.filter(ImageFilter.GaussianBlur(7))
    img.paste(sh, (mx - 12, mx - 12), sh)

    # ボタン本体: 縦方向に明→暗のグラデーション（上端にハイライト）
    cr, cg, cb = color
    for y in range(mx, mx + ih):
        t = (y - mx) / (ih - 1) if ih > 1 else 0
        # 上部ハイライト: 上端 15% で急激に明るく
        if t < 0.15:
            hf = 1.0 + (0.15 - t) / 0.15 * 0.35
        elif t < 0.4:
            hf = 1.0 - (t - 0.15) * 0.3
        else:
            hf = 1.0 - (t - 0.15) * 0.4
        r2 = min(255, max(0, int(cr * hf)))
        g2 = min(255, max(0, int(cg * hf)))
        b2 = min(255, max(0, int(cb * hf)))
        for x in range(mx + ir, w - mx - ir):
            img.putpixel((x, y), (r2, g2, b2, 255))

    # 左右の半円端
    for y in range(mx, mx + ih):
        t = (y - mx) / (ih - 1) if ih > 1 else 0
        if t < 0.15:
            hf = 1.0 + (0.15 - t) / 0.15 * 0.35
        elif t < 0.4:
            hf = 1.0 - (t - 0.15) * 0.3
        else:
            hf = 1.0 - (t - 0.15) * 0.4
        r2 = min(255, max(0, int(cr * hf)))
        g2 = min(255, max(0, int(cg * hf)))
        b2 = min(255, max(0, int(cb * hf)))
        cx_l = mx + ir
        cx_r = w - mx - ir - 1
        cy = mx + ih // 2
        for x in range(mx, mx + ir + 1):
            dx = cx_l - x
            dy = y - cy
            if dx * dx + dy * dy <= ir * ir:
                img.putpixel((x, y), (r2, g2, b2, 255))
            rx = x + (w - 2*mx - 2*ir)
            dx_r = rx - cx_r
            dy_r = y - cy
            if dx_r * dx_r + dy_r * dy_r <= ir * ir:
                img.putpixel((rx, y), (r2, g2, b2, 255))

    # 上端光沢ハイライト
    hl = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    hld = ImageDraw.Draw(hl)
    hld.rounded_rectangle((mx + 8, mx + 2, w - mx - 9, mx + ih // 3),
                          radius=ir - 4, fill=(255, 255, 255, 35))
    img.paste(hl, (0, 0), hl)

    # 縁取り
    d.rounded_rectangle((mx, mx, w - mx - 1, h - mx - 1), radius=ir,
                        outline=(min(255, cr + 30), min(255, cg + 30), min(255, cb + 30), 255),
                        width=2)

    return img


def gen_buff_icon(fg_color, shape_fn):
    """バフアイコン (96x96 ドット絵風)"""
    w, h = TEX["buff_icon"]  # 96 x 96
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 16

    # 背景: 半透明の暗い円
    cx, cy = w // 2, h // 2
    rad = 36
    d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad),
              fill=(*C_SHADOW, 200))

    # アイコン図形を描画
    shape_fn(d, cx, cy, rad - 8, fg_color)

    # 縁取り
    d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad),
              outline=fg_color, width=3)

    return img


def focus_shape(d, cx, cy, r, color):
    """集中: 星型 (☆)"""
    # 簡易的な星
    pts = []
    for i in range(5):
        angle = math.pi / 2 + i * 2 * math.pi / 5
        outer_x = cx + r * math.cos(angle)
        outer_y = cy - r * math.sin(angle)
        pts.append((outer_x, outer_y))
        inner_angle = angle + math.pi / 5
        inner_x = cx + r * 0.4 * math.cos(inner_angle)
        inner_y = cy - r * 0.4 * math.sin(inner_angle)
        pts.append((inner_x, inner_y))
    d.polygon(pts, fill=color)


def good_shape(d, cx, cy, r, color):
    """好調: 上向き矢印 (↑)"""
    pts = [
        (cx, cy - r),
        (cx + r, cy + r * 0.3),
        (cx + r * 0.35, cy + r * 0.3),
        (cx + r * 0.35, cy + r),
        (cx - r * 0.35, cy + r),
        (cx - r * 0.35, cy + r * 0.3),
        (cx - r, cy + r * 0.3),
    ]
    d.polygon(pts, fill=color)


def great_shape(d, cx, cy, r, color):
    """絶好調: 二重上向き矢印 (⇈)"""
    # 上段の矢印（小さめ）
    offset = r * 0.45
    size = r * 0.55
    pts1 = [
        (cx, cy - r),
        (cx + size, cy - r + size * 1.3),
        (cx + size * 0.35, cy - r + size * 1.3),
        (cx + size * 0.35, cy - offset),
        (cx - size * 0.35, cy - offset),
        (cx - size * 0.35, cy - r + size * 1.3),
        (cx - size, cy - r + size * 1.3),
    ]
    d.polygon(pts1, fill=color)
    # 下段の矢印
    pts2 = [
        (cx, cy - offset),
        (cx + size, cy - offset + size * 1.3),
        (cx + size * 0.35, cy - offset + size * 1.3),
        (cx + size * 0.35, cy + r),
        (cx - size * 0.35, cy + r),
        (cx - size * 0.35, cy - offset + size * 1.3),
        (cx - size, cy - offset + size * 1.3),
    ]
    d.polygon(pts2, fill=color)


def gen_tooltip_panel():
    """カード詳細ポップアップパネル"""
    w, h = TEX["tooltip"]  # 720 x 522
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 28

    body_h = int(h * (480 / 522))  # 吹き出し本体の高さ
    tail_h = h - body_h  # しっぽ部分

    # 影
    sh = Image.new("RGBA", (w + 30, h + 30), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    # 本体部分の影
    sd.rounded_rectangle((15, 15, w + 14, body_h + 14), radius=r, fill=(*C_SHADOW, 70))
    sh = sh.filter(ImageFilter.GaussianBlur(8))
    img.paste(sh, (-15, -15), sh)

    # 吹き出し本体
    d.rounded_rectangle((0, 0, w - 1, body_h - 1), radius=r,
                        fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD, width=2)

    # しっぽ (下端中央に三角形の突起)
    tail_cx = w // 2
    tail_w = 24
    d.polygon([
        (tail_cx - tail_w, body_h - 4),
        (tail_cx + tail_w, body_h - 4),
        (tail_cx, h - 1),
    ], fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD)

    # しっぽと本体の境界を消す上塗り
    d.rectangle((tail_cx - tail_w, body_h - 6, tail_cx + tail_w, body_h), fill=C_TOOLTIP_BG)

    # 上端にゴールドの細線
    d.rounded_rectangle((20, 10, w - 21, 14), radius=2, fill=C_GOLD)

    return img


# ---------------------------------------------------------------------------
# メイン
# ---------------------------------------------------------------------------

OUT_DIR = "src/main/resources/assets/gakumas_produce/textures/gui"


def main():
    import os
    os.makedirs(OUT_DIR, exist_ok=True)

    print("Generating GUI textures (学マス風再解釈版)...\n")

    # パネル
    print("  deck_editor_panel.png ...", end=" ")
    gen_panel().save(f"{OUT_DIR}/deck_editor_panel.png")
    print("OK")

    # ヘッダー
    print("  deck_editor_header.png ...", end=" ")
    gen_header().save(f"{OUT_DIR}/deck_editor_header.png")
    print("OK")

    # スロット系
    print("  card_slot.png ...", end=" ")
    gen_slot(C_SLOT_BG, C_SLOT_BORDER).save(f"{OUT_DIR}/card_slot.png")
    print("OK")

    print("  card_slot_deck.png ...", end=" ")
    gen_slot(C_SLOT_DECK_BG, (100, 88, 150)).save(f"{OUT_DIR}/card_slot_deck.png")
    print("OK")

    print("  card_slot_hover.png ...", end=" ")
    gen_slot(C_SLOT_BG, C_GOLD_BRIGHT, glow_color=C_GOLD_BRIGHT).save(f"{OUT_DIR}/card_slot_hover.png")
    print("OK")

    # ピルボタン (3色)
    print("  button_confirm.png ...", end=" ")
    gen_pill((91, 170, 107)).save(f"{OUT_DIR}/button_confirm.png")
    print("OK")

    print("  button_reset.png ...", end=" ")
    gen_pill((130, 120, 150)).save(f"{OUT_DIR}/button_reset.png")
    print("OK")

    print("  button_cancel.png ...", end=" ")
    gen_pill((224, 112, 96)).save(f"{OUT_DIR}/button_cancel.png")
    print("OK")

    # 後方互換用ニュートラルピル（使われていないが念のため）
    print("  button_pill.png ...", end=" ")
    gen_pill(C_BTN_CREAM).save(f"{OUT_DIR}/button_pill.png")
    print("OK")

    # バフアイコン
    print("  icon_focus.png ...", end=" ")
    gen_buff_icon(C_ICON_FOCUS, focus_shape).save(f"{OUT_DIR}/icon_focus.png")
    print("OK")

    print("  icon_good_condition.png ...", end=" ")
    gen_buff_icon(C_ICON_GOOD, good_shape).save(f"{OUT_DIR}/icon_good_condition.png")
    print("OK")

    print("  icon_great_condition.png ...", end=" ")
    gen_buff_icon(C_ICON_GREAT, great_shape).save(f"{OUT_DIR}/icon_great_condition.png")
    print("OK")

    # ツールチップ
    print("  tooltip_panel.png ...", end=" ")
    gen_tooltip_panel().save(f"{OUT_DIR}/tooltip_panel.png")
    print("OK")

    print(f"\nDone! All textures written to {OUT_DIR}/")


if __name__ == "__main__":
    main()
