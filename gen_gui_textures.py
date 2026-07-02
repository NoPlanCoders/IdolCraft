#!/usr/bin/env python3
"""
学マス風 GUI テクスチャ生成スクリプト v2
Minecraft Forge 1.20.1 向け gakumas_produce MOD 用。

本家学園アイドルマスターのUI特徴:
  - 深い紫〜藍色のグラデーション背景
  - 白/クリーム色のカード型パネル + ゴールドの縁取り
  - ボタンは角丸矩形（ピルではない）、立体感のあるグラデーション
  - ゴールドの細い装飾ライン
  - カード枠は金/ブラスのメタリックな質感
"""
import math
from PIL import Image, ImageDraw, ImageFilter

# =============================================================================
# カラーパレット
# =============================================================================
C_BG_DEEP      = (18, 10, 50)     # 最深背景
C_BG_MID        = (38, 22, 78)    # 中間背景
C_PANEL_FILL   = (248, 245, 255)  # 白パネル地
C_PANEL_BORDER = (150, 130, 195)  # パネル縁
C_GOLD         = (210, 165, 60)   # ゴールド
C_GOLD_BRIGHT  = (245, 205, 100)  # 明ゴールド
C_GOLD_LIGHT   = (255, 225, 150)  # ハイライトゴールド
C_GOLD_DIM     = (155, 115, 45)   # 暗ゴールド
C_HEADER_DEEP  = (22, 10, 55)     # ヘッダー背景
C_SLOT_BG      = (22, 14, 48)     # スロット背景
C_SLOT_DECK_BG = (18, 10, 40)     # デッキ側
C_SLOT_FRAME   = (190, 155, 80)   # スロット枠
C_SLOT_HOV     = (250, 215, 110)  # ホバー発光
C_SHADOW       = (8, 4, 24)
C_TEXT_DARK    = (50, 42, 65)
C_BTN_GREEN    = (80, 170, 100)
C_BTN_GRAY     = (125, 118, 148)
C_BTN_RED      = (220, 105, 90)
C_ICON_FOCUS   = (255, 200, 80)
C_ICON_GOOD    = (255, 135, 70)
C_ICON_GREAT   = (255, 70, 110)
C_TOOLTIP_BG   = (252, 250, 255)
C_TOOLTIP_BD   = (175, 160, 210)

SCALE = 3
TEX = {
    "panel":      (720 * SCALE, 540 * SCALE),   # 2160 x 1620
    "header":     (460 * SCALE, 124 * SCALE),   # 1380 x 372
    "slot":       (104 * SCALE, 104 * SCALE),   # 312 x 312
    "pill":       (234 * SCALE,  78 * SCALE),   # 702 x 234 (比3:1 → 表示78x26)
    "buff_icon":  (32 * SCALE, 32 * SCALE),     # 96 x 96
    "tooltip":    (240 * SCALE, 174 * SCALE),   # 720 x 522
}

# =============================================================================
# ユーティリティ
# =============================================================================
def rounded_rect_mask(size, radius):
    w, h = size
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, w - 1, h - 1), radius=radius, fill=255)
    return mask

def gradient_vertical(w, h, top, bottom):
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        t = y / (h - 1) if h > 1 else 0
        r = int(top[0] + (bottom[0] - top[0]) * t)
        g = int(top[1] + (bottom[1] - top[1]) * t)
        b = int(top[2] + (bottom[2] - top[2]) * t)
        for x in range(w):
            img.putpixel((x, y), (r, g, b, 255))
    return img

def blur_shadow(w, h, radius, fill_color, blur_radius):
    """ぼかし影レイヤーを作成して返す"""
    pad = blur_radius * 3
    layer = Image.new("RGBA", (w + pad * 2, h + pad * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.rounded_rectangle((pad, pad, pad + w - 1, pad + h - 1),
                        radius=radius, fill=fill_color)
    return layer.filter(ImageFilter.GaussianBlur(blur_radius))

# =============================================================================
# 生成関数
# =============================================================================

def gen_panel():
    """デッキ編成画面 背景パネル"""
    w, h = TEX["panel"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    m, r = 24, 48

    # 影
    sh = blur_shadow(w - m * 2, h - m * 2, r, (*C_SHADOW, 70), 20)
    img.paste(sh, (m - sh.width // 2 + (w - m * 2) // 2,
                    m - sh.height // 2 + (h - m * 2) // 2), sh)

    # 外枠背景 → 深紫グラデーション
    bg = gradient_vertical(w - m * 2, h - m * 2, C_BG_DEEP, C_BG_MID)
    img.paste(bg, (m, m), rounded_rect_mask(bg.size, r))

    # 内側白パネル
    im = 50
    iw, ih = w - im * 2, h - im * 2 - 16
    inner = Image.new("RGBA", (iw, ih), (0, 0, 0, 0))
    idraw = ImageDraw.Draw(inner)
    ir = 34

    # 内パネル影
    ish = blur_shadow(iw, ih, ir, (*C_SHADOW, 35), 10)
    inner.paste(ish, (-ish.width // 2 + iw // 2, -ish.height // 2 + ih // 2), ish)

    # 白地 + 紫縁 + ゴールドライン
    idraw.rounded_rectangle((0, 0, iw - 1, ih - 1), radius=ir,
                            fill=C_PANEL_FILL, outline=C_PANEL_BORDER, width=3)
    idraw.rounded_rectangle((10, 8, iw - 11, 12), radius=2, fill=C_GOLD)

    img.paste(inner, (im, im + 8), inner)

    # 四隅のゴールドアクセント
    for cx, cy in [(m + 44, m + 44), (w - m - 44, m + 44),
                   (m + 44, h - m - 44), (w - m - 44, h - m - 44)]:
        d.ellipse((cx - 7, cy - 7, cx + 7, cy + 7), fill=C_GOLD_BRIGHT)
        d.ellipse((cx - 3, cy - 3, cx + 3, cy + 3), fill=C_GOLD_LIGHT)

    return img


def gen_header():
    """タイトルヘッダー"""
    w, h = TEX["header"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 36

    # 影
    sh = blur_shadow(w, h, r, (*C_SHADOW, 95), 14)
    img.paste(sh, (-sh.width // 2 + w // 2, -sh.height // 2 + h // 2), sh)

    # 本体
    bg = gradient_vertical(w, h, C_HEADER_DEEP, C_BG_MID)
    img.paste(bg, (0, 0), rounded_rect_mask((w, h), r))

    # ゴールド装飾
    d.rounded_rectangle((36, 16, w - 36, 24), radius=4, fill=C_GOLD_DIM)
    d.rounded_rectangle((36, h - 24, w - 36, h - 16), radius=4, fill=C_GOLD_DIM)
    d.rounded_rectangle((w // 2 - 140, 32, w // 2 + 140, 37), radius=2, fill=C_GOLD_BRIGHT)

    # 左右に小さいダイヤ型のアクセント
    for ox in [48, w - 48]:
        for oy in [38, h - 38]:
            d.regular_polygon((ox, oy, 5), 4, rotation=45, fill=C_GOLD_BRIGHT)

    return img


def gen_slot(base_color, frame_color, glow_color=None):
    """カードスロット枠。margin 48px (表示16px相当) の影領域を含む"""
    w, h = TEX["slot"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    inner = int(w * (1 - 48 / 312 * 2))
    inner = inner - inner % 2
    offset = (w - inner) // 2
    r = 18

    # 影
    sh = blur_shadow(inner, inner, r, (*C_SHADOW, 110), 8)
    img.paste(sh, (offset - sh.width // 2 + inner // 2,
                    offset - sh.height // 2 + inner // 2), sh)

    # 本体背景
    d.rounded_rectangle((offset, offset, offset + inner - 1, offset + inner - 1),
                        radius=r, fill=base_color)

    # 金枠（外側）
    d.rounded_rectangle((offset - 2, offset - 2, offset + inner + 1, offset + inner + 1),
                        radius=r + 2, outline=frame_color, width=3)

    # 内側アクセント線
    d.rounded_rectangle((offset + 5, offset + 5, offset + inner - 6, offset + inner - 6),
                        radius=r - 6, outline=(*frame_color, 80), width=1)

    # ホバー発光
    if glow_color:
        gl = Image.new("RGBA", (inner + 50, inner + 50), (0, 0, 0, 0))
        gd = ImageDraw.Draw(gl)
        gd.rounded_rectangle((25, 25, inner + 24, inner + 24), radius=r + 6,
                             outline=glow_color, width=5)
        gl = gl.filter(ImageFilter.GaussianBlur(8))
        img.paste(gl, (offset - 25, offset - 25), gl)

    return img


def gen_pill(color):
    """角丸矩形ボタン。テクスチャ比3:1 → 表示78x26に歪みなくフィット"""
    w, h = TEX["pill"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    mx = 12   # マージン
    iw, ih = w - mx * 2, h - mx * 2
    cr, cg, cb = color
    radius = 28  # 角丸半径（表示で約9.3px）

    # 影
    sh = blur_shadow(iw, ih, radius, (*C_SHADOW, 100), 10)
    img.paste(sh, (mx - sh.width // 2 + iw // 2,
                    mx - sh.height // 2 + ih // 2), sh)

    # 本体グラデーション: 上端ハイライト → 徐々に暗く
    for y in range(mx, mx + ih):
        t = (y - mx) / (ih - 1) if ih > 1 else 0
        if t < 0.22:
            hf = 1.0 + (0.22 - t) / 0.22 * 0.30
        else:
            hf = 1.0 - (t - 0.22) * 0.35
        r2 = min(255, max(0, int(cr * hf)))
        g2 = min(255, max(0, int(cg * hf)))
        b2 = min(255, max(0, int(cb * hf)))
        for x in range(mx + radius, w - mx - radius):
            img.putpixel((x, y), (r2, g2, b2, 255))

    # 四隅
    for y in range(mx, mx + ih):
        t = (y - mx) / (ih - 1) if ih > 1 else 0
        if t < 0.22:
            hf = 1.0 + (0.22 - t) / 0.22 * 0.30
        else:
            hf = 1.0 - (t - 0.22) * 0.35
        r2 = min(255, max(0, int(cr * hf)))
        g2 = min(255, max(0, int(cg * hf)))
        b2 = min(255, max(0, int(cb * hf)))
        for cx in [mx + radius, w - mx - radius - 1]:
            for ox in range(-radius, radius + 1):
                px = cx + ox
                if px < mx or px >= w - mx:
                    continue
                dy = y - (mx + ih // 2)
                if ox * ox + dy * dy <= radius * radius:
                    img.putpixel((px, y), (r2, g2, b2, 255))

    # 上端ハイライト
    hl = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    hld = ImageDraw.Draw(hl)
    hld.rounded_rectangle((mx + 6, mx + 3, w - mx - 7, mx + ih // 3),
                           radius=radius - 8, fill=(255, 255, 255, 30))
    img.paste(hl, (0, 0), hl)

    # 縁取り
    d.rounded_rectangle((mx, mx, w - mx - 1, h - mx - 1), radius=radius,
                        outline=(min(255, cr + 35), min(255, cg + 35), min(255, cb + 35), 255),
                        width=2)

    return img


def gen_buff_icon(fg_color, shape_fn):
    """バフアイコン"""
    w, h = TEX["buff_icon"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy = w // 2, h // 2
    rad = 34

    d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad), fill=(*C_SHADOW, 200))
    shape_fn(d, cx, cy, rad - 8, fg_color)
    d.ellipse((cx - rad, cy - rad, cx + rad, cy + rad), outline=fg_color, width=3)

    return img


def focus_shape(d, cx, cy, r, color):
    pts = []
    for i in range(5):
        a = math.pi / 2 + i * 2 * math.pi / 5
        o_x, o_y = cx + r * math.cos(a), cy - r * math.sin(a)
        pts.append((o_x, o_y))
        ia = a + math.pi / 5
        i_x, i_y = cx + r * 0.4 * math.cos(ia), cy - r * 0.4 * math.sin(ia)
        pts.append((i_x, i_y))
    d.polygon(pts, fill=color)


def good_shape(d, cx, cy, r, color):
    pts = [
        (cx, cy - r),
        (cx + r, cy + int(r * 0.3)),
        (cx + int(r * 0.35), cy + int(r * 0.3)),
        (cx + int(r * 0.35), cy + r),
        (cx - int(r * 0.35), cy + r),
        (cx - int(r * 0.35), cy + int(r * 0.3)),
        (cx - r, cy + int(r * 0.3)),
    ]
    d.polygon(pts, fill=color)


def great_shape(d, cx, cy, r, color):
    offset = int(r * 0.45)
    size = int(r * 0.55)
    def arrow(oy):
        return [
            (cx, oy - size),
            (cx + size, oy),
            (cx + int(size * 0.35), oy),
            (cx + int(size * 0.35), oy + offset + size),
            (cx - int(size * 0.35), oy + offset + size),
            (cx - int(size * 0.35), oy),
            (cx - size, oy),
        ]
    d.polygon(arrow(cy - offset), fill=color)
    d.polygon(arrow(cy + offset), fill=color)


def gen_tooltip_panel():
    """カード詳細ツールチップ"""
    w, h = TEX["tooltip"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 26
    body_h = int(h * (480 / 522))

    # 影
    sh = blur_shadow(w, body_h, r, (*C_SHADOW, 65), 9)
    img.paste(sh, (-sh.width // 2 + w // 2, -sh.height // 2 + body_h // 2), sh)

    # 本体
    d.rounded_rectangle((0, 0, w - 1, body_h - 1), radius=r,
                        fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD, width=2)

    # しっぽ
    cx, tw = w // 2, 22
    d.polygon([(cx - tw, body_h - 3), (cx + tw, body_h - 3), (cx, h - 1)],
              fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD)
    d.rectangle((cx - tw, body_h - 5, cx + tw, body_h + 1), fill=C_TOOLTIP_BG)

    # ゴールド線
    d.rounded_rectangle((18, 9, w - 19, 13), radius=2, fill=C_GOLD)

    return img


# =============================================================================
# メイン
# =============================================================================
OUT_DIR = "src/main/resources/assets/gakumas_produce/textures/gui"

def main():
    import os
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generating GUI textures (学マス本家寄せ v2)...\n")

    print("  deck_editor_panel.png ...", end=" ")
    gen_panel().save(f"{OUT_DIR}/deck_editor_panel.png")
    print("OK")

    print("  deck_editor_header.png ...", end=" ")
    gen_header().save(f"{OUT_DIR}/deck_editor_header.png")
    print("OK")

    print("  card_slot.png ...", end=" ")
    gen_slot(C_SLOT_BG, C_SLOT_FRAME).save(f"{OUT_DIR}/card_slot.png")
    print("OK")

    print("  card_slot_deck.png ...", end=" ")
    gen_slot(C_SLOT_DECK_BG, (100, 85, 155)).save(f"{OUT_DIR}/card_slot_deck.png")
    print("OK")

    print("  card_slot_hover.png ...", end=" ")
    gen_slot(C_SLOT_BG, C_GOLD_BRIGHT, glow_color=C_SLOT_HOV).save(f"{OUT_DIR}/card_slot_hover.png")
    print("OK")

    print("  button_confirm.png ...", end=" ")
    gen_pill(C_BTN_GREEN).save(f"{OUT_DIR}/button_confirm.png")
    print("OK")

    print("  button_reset.png ...", end=" ")
    gen_pill(C_BTN_GRAY).save(f"{OUT_DIR}/button_reset.png")
    print("OK")

    print("  button_cancel.png ...", end=" ")
    gen_pill(C_BTN_RED).save(f"{OUT_DIR}/button_cancel.png")
    print("OK")

    print("  button_pill.png ...", end=" ")
    gen_pill(C_BTN_GRAY).save(f"{OUT_DIR}/button_pill.png")
    print("OK")

    print("  icon_focus.png ...", end=" ")
    gen_buff_icon(C_ICON_FOCUS, focus_shape).save(f"{OUT_DIR}/icon_focus.png")
    print("OK")

    print("  icon_good_condition.png ...", end=" ")
    gen_buff_icon(C_ICON_GOOD, good_shape).save(f"{OUT_DIR}/icon_good_condition.png")
    print("OK")

    print("  icon_great_condition.png ...", end=" ")
    gen_buff_icon(C_ICON_GREAT, great_shape).save(f"{OUT_DIR}/icon_great_condition.png")
    print("OK")

    print("  tooltip_panel.png ...", end=" ")
    gen_tooltip_panel().save(f"{OUT_DIR}/tooltip_panel.png")
    print("OK")

    print(f"\nDone! → {OUT_DIR}/")


if __name__ == "__main__":
    main()
