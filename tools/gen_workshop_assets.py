#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""スキルカード作業台とレア度別素材のアセット生成（テクスチャ/モデル/blockstate/loot/recipe/lang）。"""
import json
import os
from PIL import Image, ImageDraw

BASE = "src/main/resources"
A = f"{BASE}/assets/gakumas_produce"
D = f"{BASE}/data/gakumas_produce"

# id, 日本語, 英語, レア色, クラフト追加素材
MATERIALS = [
    ("card_material_white",  "白カードの素材", "White Card Material",  (196, 202, 214), "minecraft:string"),
    ("card_material_silver", "銀カードの素材", "Silver Card Material", (176, 194, 216), "minecraft:iron_ingot"),
    ("card_material_gold",   "金カードの素材", "Gold Card Material",   (232, 194, 90),  "minecraft:gold_ingot"),
    ("card_material_rainbow","虹カードの素材", "Rainbow Card Material",(228, 102, 176), "minecraft:diamond"),
]
SIZE = 64


def ensure(*ds):
    for d in ds:
        os.makedirs(d, exist_ok=True)


def gen_material_texture(cid, color):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    dark = tuple(max(0, int(c * 0.68)) for c in color)
    light = tuple(min(255, int(c + (255 - c) * 0.5)) for c in color)
    cx, cy = 32, 33
    # ダイヤ型のクリスタル素材
    top, bot, half = cy - 22, cy + 24, 17
    d.polygon([(cx, top), (cx + half, cy), (cx, bot), (cx - half, cy)], fill=color, outline=dark)
    # 上面ハイライト
    d.polygon([(cx, top), (cx + half, cy), (cx, cy)], fill=light)
    # ファセット線
    d.line([(cx, top), (cx, bot)], fill=dark, width=1)
    d.line([(cx - half, cy), (cx + half, cy)], fill=dark, width=1)
    img.save(f"{A}/textures/item/{cid}.png")


def gen_workshop_texture():
    """作業台ブロックのテクスチャ（白い机＋カードのカラーアクセント）"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # 木目調のベース
    d.rectangle((0, 0, 15, 15), fill=(232, 224, 210))
    d.rectangle((0, 0, 15, 3), fill=(214, 204, 186))
    for y in (6, 10, 13):
        d.line((0, y, 15, y), fill=(210, 200, 182))
    # 天面にCMYのカード3枚
    for i, col in enumerate([(58, 178, 236), (242, 96, 152), (248, 196, 66)]):
        x = 2 + i * 4
        d.rectangle((x, 4, x + 3, 9), fill=col, outline=(255, 255, 255))
    img.save(f"{A}/textures/block/card_workshop.png")


def write(path, obj):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)


def gen_material_assets():
    for cid, jp, en, color, mat in MATERIALS:
        gen_material_texture(cid, color)
        write(f"{A}/models/item/{cid}.json",
              {"parent": "item/generated", "textures": {"layer0": f"gakumas_produce:item/{cid}"}})
        write(f"{D}/recipes/{cid}.json", {
            "type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": "minecraft:paper"}, {"item": "minecraft:paper"},
                            {"item": "minecraft:paper"}, {"item": mat}],
            "result": {"item": f"gakumas_produce:{cid}", "count": 1},
        })


def gen_block_assets():
    write(f"{A}/blockstates/card_workshop.json",
          {"variants": {"": {"model": "gakumas_produce:block/card_workshop"}}})
    write(f"{A}/models/block/card_workshop.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": "gakumas_produce:block/card_workshop"}})
    write(f"{A}/models/item/card_workshop.json", {"parent": "gakumas_produce:block/card_workshop"})
    write(f"{D}/loot_tables/blocks/card_workshop.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "gakumas_produce:card_workshop"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write(f"{D}/recipes/card_workshop.json", {
        "type": "minecraft:crafting_shaped",
        "pattern": ["PPP", "PCP", "PPP"],
        "key": {"P": {"item": "minecraft:paper"}, "C": {"item": "minecraft:crafting_table"}},
        "result": {"item": "gakumas_produce:card_workshop", "count": 1},
    })


def update_lang(fname, idx):
    p = f"{A}/lang/{fname}"
    data = json.load(open(p, encoding="utf-8"))
    for cid, jp, en, _, _ in MATERIALS:
        data[f"item.gakumas_produce.{cid}"] = jp if idx == 0 else en
    data["block.gakumas_produce.card_workshop"] = "スキルカード作業台" if idx == 0 else "Skill Card Workshop"
    json.dump(data, open(p, "w", encoding="utf-8"), ensure_ascii=False, indent=2)


def main():
    ensure(f"{A}/textures/item", f"{A}/textures/block", f"{A}/models/item", f"{A}/models/block",
           f"{A}/blockstates", f"{D}/recipes", f"{D}/loot_tables/blocks")
    gen_material_assets()
    gen_workshop_texture()
    gen_block_assets()
    update_lang("ja_jp.json", 0)
    update_lang("en_us.json", 1)
    print("generated materials + workshop block assets")


if __name__ == "__main__":
    main()
