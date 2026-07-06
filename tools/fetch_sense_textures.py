#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
本家学マス wiki (wikiwiki.jp/gakumas スキルカード一覧) から
センスプランの新規カードのアイコン画像を取得し、item テクスチャ(PNG)として保存する。

カード名→画像URLは、一覧ページHTML内の <img ... alt="カード名"> から対応付ける。
alt にはファイル拡張子(.jpg等)やHTMLエンティティ・"nolink,"接頭辞が混じることがあるため正規化する。
"""
import html
import re
import sys
import urllib.request
from io import BytesIO
from PIL import Image

LIST_URL = "https://wikiwiki.jp/gakumas/%E3%82%B9%E3%82%AD%E3%83%AB%E3%82%AB%E3%83%BC%E3%83%89%E4%B8%80%E8%A6%A7"
OUT_DIR = "src/main/resources/assets/idolcraft/textures/item"
TEX_SIZE = 96  # item テクスチャの一辺(px)

# id -> 本家カード名（画像検索キー）。新規センスカード50種。
ROSTER = {
    # 白(N)
    "card_challenge": "挑戦",
    "card_trial_error": "試行錯誤",
    "card_gaze_basic": "視線の基本",
    "card_thinking_basic": "思考の基本",
    "card_composure_basic": "落ち着きの基本",
    "card_timing_basic": "タイミングの基本",
    # 銀(SR)
    "card_light_steps": "軽い足取り",
    "card_charm": "愛嬌",
    "card_warmup": "準備運動",
    "card_fan_service": "ファンサ",
    "card_momentum": "勢い任せ",
    "card_high_touch": "ハイタッチ",
    "card_talk_time": "トークタイム",
    "card_course_correction": "軌道修正",
    "card_pump_up": "パンプアップ",
    "card_pacing": "ペース配分",
    "card_balance_sense": "バランス感覚",
    "card_optimistic": "楽観的",
    "card_deep_breath": "深呼吸",
    "card_one_breath": "ひと呼吸",
    # 金(SSR)
    "card_decided_pose": "決めポーズ",
    "card_adlib": "アドリブ",
    "card_passion_turn": "情熱ターン",
    "card_leap": "飛躍",
    "card_blessing": "祝福",
    "card_start_dash": "スタートダッシュ",
    "card_stand_play": "スタンドプレー",
    "card_position_check": "立ち位置チェック",
    "card_unstoppable": "破竹の勢い",
    "card_keen_eye": "眼力",
    "card_big_cheer": "大声援",
    "card_power_of_wish": "願いの力",
    "card_starting_signal": "始まりの合図",
    "card_grit": "意地",
    "card_path_to_success": "成功への道筋",
    "card_spotlight": "スポットライト",
    "card_one_shot": "一発勝負",
    "card_thrilling": "スリリング",
    "card_fearless": "大胆不敵",
    "card_mental_unity": "精神統一",
    # 虹(UR)
    "card_buzzword": "バズワード",
    "card_fulfillment": "成就",
    "card_charming_performance": "魅惑のパフォーマンス",
    "card_supreme_entertainment": "至高のエンタメ",
    "card_awakening": "覚醒",
    "card_limelight": "脚光",
    "card_hot_topic": "話題沸騰",
    "card_national_idol": "国民的アイドル",
    "card_endless_applause": "鳴り止まない拍手",
    "card_natural_talent": "天賦の才",
}


def normalize(name: str) -> str:
    # 一部の alt は数値文字参照が二重エスケープ(&amp;#12473;等)されているため2回 unescape する
    name = html.unescape(html.unescape(name))
    name = name.replace("nolink,", "")
    name = re.sub(r"\.(jpg|jpeg|png|webp)$", "", name, flags=re.IGNORECASE)
    name = re.sub(r"_\d+$", "", name)  # 末尾 _0 等
    name = name.strip()
    name = name.lstrip(";,#")  # 先頭に残ったエスケープ由来の記号を除去
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
            # あいまい一致: 正規化キーが部分一致するものを探す
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

