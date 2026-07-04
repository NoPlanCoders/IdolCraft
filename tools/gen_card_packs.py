#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""レア度別カードパックのアセット生成: テクスチャ(PIL)・モデル・lang・クラフトレシピ。"""
import json
import os
from PIL import Image, ImageDraw

BASE = "src/main/resources"
TEX = f"{BASE}/assets/gakumas_produce/textures/item"
MDL = f"{BASE}/assets/gakumas_produce/models/item"
LANG = f"{BASE}/assets/gakumas_produce/lang"
REC = f"{BASE}/data/gakumas_produce/recipes"

# id, 日本語, 英語, レア色(R,G,B), クラフト追加素材(材料アイテム)
PACKS = [
    ("card_pack_white",  "白カードパック", "White Card Pack",  (196, 202, 214), "minecraft:string"),
    ("card_pack_silver", "銀カードパック", "Silver Card Pack", (176, 194, 216), "minecraft:iron_ingot"),
    ("card_pack_gold",   "金カードパック", "Gold Card Pack",   (232, 194, 90),  "minecraft:gold_ingot"),
    ("card_pack_rainbow","虹カードパック", "Rainbow Card Pack",(228, 102, 176), "minecraft:diamond"),
]

SIZE = 64


def rounded(draw, box, r, **kw):
    draw.rounded_rectangle(box, radius=r, **kw)


def gen_texture(cid, color):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cr, cg, cb = color
    dark = tuple(max(0, int(c * 0.72)) for c in color)
    light = tuple(min(255, int(c + (255 - c) * 0.45)) for c in color)
    # パック本体（縦長の封筒/パック）
    x0, y0, x1, y1 = 14, 8, 50, 56
    d.rectangle((x0 + 3, y1 - 3, x1 + 3, y1 + 1), fill=(0, 0, 0, 60))  # 影
    rounded(d, (x0, y0, x1, y1), 6, fill=color, outline=dark, width=2)
    # 上部の帯（開封口）
    rounded(d, (x0, y0, x1, y0 + 12), 6, fill=light)
    d.line((x0 + 2, y0 + 12, x1 - 2, y0 + 12), fill=dark, width=1)
    # 中央のきらめき（星）
    cx, cy, s = 32, 34, 9
    d.polygon([(cx, cy - s), (cx + 3, cy - 3), (cx + s, cy), (cx + 3, cy + 3),
               (cx, cy + s), (cx - 3, cy + 3), (cx - s, cy), (cx - 3, cy - 3)],
              fill=(255, 255, 255, 235))
    img.save(f"{TEX}/{cid}.png")


def gen_model(cid):
    with open(f"{MDL}/{cid}.json", "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated",
                   "textures": {"layer0": f"gakumas_produce:item/{cid}"}}, f, ensure_ascii=False, indent=2)


def gen_recipe(cid, material):
    recipe = {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [
            {"item": "minecraft:paper"},
            {"item": "minecraft:paper"},
            {"item": "minecraft:paper"},
            {"item": material},
        ],
        "result": {"item": f"gakumas_produce:{cid}", "count": 1},
    }
    with open(f"{REC}/{cid}.json", "w", encoding="utf-8") as f:
        json.dump(recipe, f, ensure_ascii=False, indent=2)


def update_lang(fname, idx):
    path = f"{LANG}/{fname}"
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    for cid, jp, en, _, _ in PACKS:
        data[f"item.gakumas_produce.{cid}"] = jp if idx == 0 else en
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def main():
    for d in (TEX, MDL, REC):
        os.makedirs(d, exist_ok=True)
    for cid, jp, en, color, mat in PACKS:
        gen_texture(cid, color)
        gen_model(cid)
        gen_recipe(cid, mat)
    update_lang("ja_jp.json", 0)
    update_lang("en_us.json", 1)
    print(f"generated {len(PACKS)} packs: textures, models, recipes, lang")


if __name__ == "__main__":
    main()
