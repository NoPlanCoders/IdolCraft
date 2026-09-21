#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
本家学マス wiki (wikiwiki.jp/gakumas スキルカード一覧) から
フリー／ロジック／アノマリー新規カードのアイコン画像を取得し、item テクスチャ(PNG)として保存する。
（fetch_sense_textures.py と同じ手法。ROSTER は gen_new_plans_boilerplate.py の CARDS から生成）
"""
import html
import re
import sys
import urllib.request
from io import BytesIO
from PIL import Image

sys.path.insert(0, "tools")
from gen_new_plans_boilerplate import CARDS

LIST_URL = "https://wikiwiki.jp/gakumas/%E3%82%B9%E3%82%AD%E3%83%AB%E3%82%AB%E3%83%BC%E3%83%89%E4%B8%80%E8%A6%A7"
OUT_DIR = "src/main/resources/assets/idolcraft/textures/item"
TEX_SIZE = 96

# トラブルカード（眠気）は wiki のスキルカード一覧に無いため取得対象から除外する
ROSTER = {cid: jp for cid, jp, _, rarity in CARDS if rarity is not None}


def normalize(name: str) -> str:
    name = html.unescape(html.unescape(name))
    name = name.replace("nolink,", "")
    name = re.sub(r"\.(jpg|jpeg|png|webp)$", "", name, flags=re.IGNORECASE)
    name = re.sub(r"_\d+$", "", name)
    name = name.strip()
    name = name.lstrip(";,#")
    return name.strip()


def build_name_to_url(html_text: str) -> dict:
    pairs = re.findall(r'<img src="(https://cdn\.wikiwiki\.jp[^"]+?)"[^>]*?alt="([^"]*)"', html_text)
    mapping = {}
    for url, alt in pairs:
        if "icon_" in url:
            continue
        key = normalize(alt)
        if key and key not in mapping:
            mapping[key] = html.unescape(url)
    return mapping


def main():
    import os
    os.makedirs(OUT_DIR, exist_ok=True)

    print("Fetching card list HTML ...")
    req = urllib.request.Request(LIST_URL, headers={"User-Agent": "Mozilla/5.0"})
    html_text = urllib.request.urlopen(req, timeout=30).read().decode("utf-8", "replace")
    name_to_url = build_name_to_url(html_text)
    print(f"  parsed {len(name_to_url)} name->url entries\n")

    ok, missing = [], []
    for card_id, jp in ROSTER.items():
        key = normalize(jp)
        url = name_to_url.get(key)
        if not url:
            for k, u in name_to_url.items():
                if key and (key in k or k in key):
                    url = u
                    break
        if not url:
            missing.append((card_id, jp))
            print(f"  [MISS] {card_id} ({jp}) : no image URL found")
            continue
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            raw = urllib.request.urlopen(req, timeout=30).read()
            im = Image.open(BytesIO(raw)).convert("RGBA")
            im = im.resize((TEX_SIZE, TEX_SIZE), Image.LANCZOS)
            im.save(f"{OUT_DIR}/{card_id}.png")
            ok.append(card_id)
            print(f"  [OK]   {card_id} ({jp}) <- {im.size}")
        except Exception as e:
            missing.append((card_id, jp))
            print(f"  [ERR]  {card_id} ({jp}) : {e}")

    print(f"\nDone. {len(ok)} saved, {len(missing)} missing.")
    if missing:
        print("Missing:")
        for cid, jp in missing:
            print(f"  {cid} : {jp}")


if __name__ == "__main__":
    main()
