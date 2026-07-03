#!/usr/bin/env python3
"""
学マス風 GUI テクスチャ生成スクリプト v3 ─ 白ベース + すりガラス + CMY
Minecraft Forge 1.20.1 向け gakumas_produce MOD 用。

本家学園アイドルマスターのUI特徴を反映:
  - 白ベースの背景に自然なソフトシャドウ（真っ黒ではなく青みグレーの影）
  - すりガラス（グラスモーフィズム）風の半透明パネル + 白い縁でガラスの厚みを表現
  - フラット2.0：角丸を基調にグラデーションを多用
  - パラメータ/アクセントは青(C)・ピンク(M)・黄(Y)の3色
  - 大きめの単色アイコン、装飾は最小限
"""
import math
from PIL import Image, ImageDraw, ImageFilter

# =============================================================================
# カラーパレット（白ベース + すりガラス + CMY）
# =============================================================================
# 白/ガラス
C_PANEL_FILL    = (255, 255, 255)   # 純白パネル地
C_PANEL_FILL2   = (245, 248, 253)   # わずかに青みの白（グラデ下端）
C_GLASS_TINT_T  = (255, 255, 255)   # すりガラス上（ほぼ白）
C_GLASS_TINT_B  = (240, 244, 252)   # すりガラス下（淡い青白）
C_WHITE_EDGE    = (255, 255, 255)   # ガラスの白い厚み枠
C_EDGE_SOFT     = (223, 226, 238)   # 外縁の淡いグレーラベンダー
C_SHADOW        = (58, 70, 110)     # 影（真っ黒を避け青みグレー）

# CMY アクセント（青・ピンク・黄）
C_CYAN          = (58, 178, 236)
C_CYAN_DK       = (38, 150, 212)
C_PINK          = (242, 96, 152)
C_PINK_DK       = (214, 72, 128)
C_YELLOW        = (248, 196, 66)
C_YELLOW_DK     = (226, 168, 40)

# スロット
C_SLOT_BG       = (255, 255, 255)   # 一覧側 白
C_SLOT_DECK_BG  = (243, 247, 253)   # デッキ側 淡い青白
C_SLOT_FRAME    = (214, 219, 233)   # 通常枠 淡いラベンダーグレー
C_SLOT_HOV      = C_PINK            # ホバー発光 ピンク

# ボタン（白テキストが乗る前提の彩度）
C_BTN_CONFIRM   = C_PINK            # 確定 = ピンク（主要CTA）
C_BTN_RESET     = (150, 158, 182)   # 全削除 = グレーブルー
C_BTN_CANCEL    = (120, 127, 150)   # キャンセル = グレー

# バフアイコン（意味色をCMYに整理: 集中=青 / 好調=黄 / 絶好調=ピンク）
C_ICON_FOCUS    = C_CYAN
C_ICON_GOOD     = C_YELLOW
C_ICON_GREAT    = C_PINK

# ツールチップ
C_TOOLTIP_BG    = (255, 255, 255)
C_TOOLTIP_BD    = (226, 228, 240)

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

def lighten(c, f):
    return tuple(min(255, int(c[i] + (255 - c[i]) * f)) for i in range(3))

def darken(c, f):
    return tuple(max(0, int(c[i] * (1 - f))) for i in range(3))

def cmy_accent_line(d, x0, x1, y, thickness, radius=2):
    """青→ピンク→黄 の3分割アクセントライン（学マスのCMYパラメータ色）"""
    span = x1 - x0
    seg = span // 3
    d.rounded_rectangle((x0, y, x0 + seg, y + thickness), radius=radius, fill=C_CYAN)
    d.rectangle((x0 + seg, y, x0 + seg * 2, y + thickness), fill=C_PINK)
    d.rounded_rectangle((x0 + seg * 2, y, x1, y + thickness), radius=radius, fill=C_YELLOW)

# =============================================================================
# 生成関数
# =============================================================================

def gen_panel():
    """デッキ編成画面 背景パネル（白いすりガラスカード）"""
    w, h = TEX["panel"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    m, r = 30, 46

    # ソフトシャドウ（青みグレー、広く淡く）
    sh = blur_shadow(w - m * 2, h - m * 2, r, (*C_SHADOW, 60), 22)
    img.paste(sh, (m - sh.width // 2 + (w - m * 2) // 2,
                    m - sh.height // 2 + (h - m * 2) // 2), sh)

    # すりガラス本体（上ほぼ白 → 下 淡い青白のグラデ）
    bg = gradient_vertical(w - m * 2, h - m * 2, C_GLASS_TINT_T, C_GLASS_TINT_B)
    mask = rounded_rect_mask(bg.size, r)
    img.paste(bg, (m, m), mask)

    # ガラスの厚みを表す内側の白いハイライト枠 + 淡い外縁
    d.rounded_rectangle((m, m, w - m - 1, h - m - 1), radius=r,
                        outline=(*C_EDGE_SOFT, 255), width=3)
    d.rounded_rectangle((m + 4, m + 4, w - m - 5, h - m - 5), radius=r - 4,
                        outline=(255, 255, 255, 210), width=3)

    # 上部にCMYアクセントライン
    cmy_accent_line(d, m + 60, w - m - 60, m + 30, 9, radius=4)

    return img


def gen_header():
    """タイトルヘッダー（白い角丸バー + CMY下線）"""
    w, h = TEX["header"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 40

    # ソフトシャドウ
    sh = blur_shadow(w - 24, h - 24, r, (*C_SHADOW, 70), 14)
    img.paste(sh, (-sh.width // 2 + w // 2, -sh.height // 2 + h // 2), sh)

    # 白本体
    d.rounded_rectangle((12, 12, w - 13, h - 13), radius=r, fill=C_PANEL_FILL,
                        outline=C_EDGE_SOFT, width=3)
    # 上端の白ハイライト（ガラス厚み）
    d.rounded_rectangle((18, 18, w - 19, h // 2), radius=r - 8,
                        outline=(255, 255, 255, 180), width=2)

    # 下部にCMYアクセントライン
    cmy_accent_line(d, 60, w - 60, h - 34, 10, radius=4)

    return img


def gen_slot(base_color, frame_color, glow_color=None):
    """カードスロット枠。margin 48px (表示16px相当) の影領域を含む"""
    w, h = TEX["slot"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    inner = int(w * (1 - 48 / 312 * 2))
    inner = inner - inner % 2
    offset = (w - inner) // 2
    r = 20

    # ソフトシャドウ（薄め）
    sh = blur_shadow(inner, inner, r, (*C_SHADOW, 70), 9)
    img.paste(sh, (offset - sh.width // 2 + inner // 2,
                    offset - sh.height // 2 + inner // 2), sh)

    # 本体背景（白 / 淡い青白）
    d.rounded_rectangle((offset, offset, offset + inner - 1, offset + inner - 1),
                        radius=r, fill=base_color, outline=frame_color, width=3)

    # 内側の白ハイライト（ガラス厚み）
    d.rounded_rectangle((offset + 5, offset + 5, offset + inner - 6, offset + inner - 6),
                        radius=r - 5, outline=(255, 255, 255, 170), width=2)

    # ホバー発光（ピンクのソフトグロー + 実線枠）
    if glow_color:
        gl = Image.new("RGBA", (inner + 60, inner + 60), (0, 0, 0, 0))
        gd = ImageDraw.Draw(gl)
        gd.rounded_rectangle((30, 30, inner + 29, inner + 29), radius=r + 8,
                             outline=(*glow_color, 230), width=7)
        gl = gl.filter(ImageFilter.GaussianBlur(9))
        img.paste(gl, (offset - 30, offset - 30), gl)
        # くっきりした内枠も重ねる
        d.rounded_rectangle((offset, offset, offset + inner - 1, offset + inner - 1),
                            radius=r, outline=glow_color, width=3)

    return img


def gen_pill(color):
    """角丸矩形ボタン。テクスチャ比3:1 → 表示78x26に歪みなくフィット"""
    w, h = TEX["pill"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    mx = 12   # マージン
    iw, ih = w - mx * 2, h - mx * 2
    cr, cg, cb = color
    radius = 30

    # ソフトシャドウ
    sh = blur_shadow(iw, ih, radius, (*C_SHADOW, 85), 11)
    img.paste(sh, (mx - sh.width // 2 + iw // 2,
                    mx - sh.height // 2 + ih // 2), sh)

    # 本体グラデ（上明るめ → 下やや暗め。フラット2.0のなだらかグラデ）
    body = Image.new("RGBA", (iw, ih), (0, 0, 0, 0))
    for y in range(ih):
        t = y / (ih - 1) if ih > 1 else 0
        hf = 1.08 - 0.20 * t   # 上1.08倍 → 下0.88倍
        r2 = min(255, max(0, int(cr * hf)))
        g2 = min(255, max(0, int(cg * hf)))
        b2 = min(255, max(0, int(cb * hf)))
        for x in range(iw):
            body.putpixel((x, y), (r2, g2, b2, 255))
    img.paste(body, (mx, mx), rounded_rect_mask((iw, ih), radius))

    # 上端の白ハイライト（つやのある立体感）
    hl = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    hld = ImageDraw.Draw(hl)
    hld.rounded_rectangle((mx + 8, mx + 4, w - mx - 9, mx + ih // 2 - 2),
                           radius=radius - 8, fill=(255, 255, 255, 55))
    img.paste(hl, (0, 0), hl)

    # 縁取り（明るめの同系色）
    d.rounded_rectangle((mx, mx, w - mx - 1, h - mx - 1), radius=radius,
                        outline=(min(255, cr + 30), min(255, cg + 30), min(255, cb + 30), 255),
                        width=2)

    return img


def gen_buff_icon(base_color, shape_fn):
    """バフアイコン（本家学マス風: 角丸ひし形＝ダイヤ型 + グラデ + 白グリフ + 白リム）。
    ダイヤ背景だけを45°回転させ、グリフは正立で上描きする。"""
    w, h = TEX["buff_icon"]   # 96 x 96
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))

    side = 58
    rad = 15

    # 角丸スクエアにグラデ（明→暗）を敷き、白リムを重ねる
    sq = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    grad = gradient_vertical(side, side, lighten(base_color, 0.32), darken(base_color, 0.14))
    sq.paste(grad, (0, 0), rounded_rect_mask((side, side), rad))
    ImageDraw.Draw(sq).rounded_rectangle((2, 2, side - 3, side - 3), radius=rad - 2,
                                         outline=(255, 255, 255, 235), width=3)

    # 45°回転してダイヤ型に
    dia = sq.rotate(45, expand=True, resample=Image.BICUBIC)
    ox = (w - dia.width) // 2
    oy = (h - dia.height) // 2

    # ソフトシャドウ（ダイヤのアルファから生成）
    alpha = dia.split()[3]
    shadow = Image.new("RGBA", dia.size, (*C_SHADOW, 0))
    shadow.putalpha(alpha.point(lambda a: int(a * 0.45)))
    shadow = shadow.filter(ImageFilter.GaussianBlur(5))
    img.paste(shadow, (ox, oy + 3), shadow)

    # ダイヤ本体
    img.paste(dia, (ox, oy), dia)

    # 正立の白グリフ
    cx, cy = w // 2, h // 2
    shape_fn(ImageDraw.Draw(img), cx, cy, 22, (255, 255, 255, 255))

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
    """カード詳細ツールチップ（白いすりガラス吹き出し）"""
    w, h = TEX["tooltip"]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = 28
    body_h = int(h * (480 / 522))

    # ソフトシャドウ
    sh = blur_shadow(w - 16, body_h, r, (*C_SHADOW, 55), 10)
    img.paste(sh, (-sh.width // 2 + w // 2, -sh.height // 2 + body_h // 2), sh)

    # 本体
    d.rounded_rectangle((0, 0, w - 1, body_h - 1), radius=r,
                        fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD, width=2)
    # 内側の白ハイライト枠
    d.rounded_rectangle((5, 5, w - 6, body_h - 6), radius=r - 5,
                        outline=(255, 255, 255, 190), width=2)

    # しっぽ
    cx, tw = w // 2, 22
    d.polygon([(cx - tw, body_h - 3), (cx + tw, body_h - 3), (cx, h - 1)],
              fill=C_TOOLTIP_BG, outline=C_TOOLTIP_BD)
    d.rectangle((cx - tw + 1, body_h - 6, cx + tw - 1, body_h + 1), fill=C_TOOLTIP_BG)

    # CMYアクセントライン（上部）
    cmy_accent_line(d, 20, w - 20, 11, 6, radius=3)

    return img


# =============================================================================
# メイン
# =============================================================================
OUT_DIR = "src/main/resources/assets/gakumas_produce/textures/gui"

def main():
    import os
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generating GUI textures (学マス風 v3: 白ベース + すりガラス + CMY)...\n")

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
    gen_slot(C_SLOT_DECK_BG, (200, 210, 228)).save(f"{OUT_DIR}/card_slot_deck.png")
    print("OK")

    print("  card_slot_hover.png ...", end=" ")
    gen_slot(C_SLOT_BG, C_PINK, glow_color=C_SLOT_HOV).save(f"{OUT_DIR}/card_slot_hover.png")
    print("OK")

    print("  button_confirm.png ...", end=" ")
    gen_pill(C_BTN_CONFIRM).save(f"{OUT_DIR}/button_confirm.png")
    print("OK")

    print("  button_reset.png ...", end=" ")
    gen_pill(C_BTN_RESET).save(f"{OUT_DIR}/button_reset.png")
    print("OK")

    print("  button_cancel.png ...", end=" ")
    gen_pill(C_BTN_CANCEL).save(f"{OUT_DIR}/button_cancel.png")
    print("OK")

    print("  button_pill.png ...", end=" ")
    gen_pill(C_BTN_RESET).save(f"{OUT_DIR}/button_pill.png")
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
