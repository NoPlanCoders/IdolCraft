#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
センス新規50カードの定型ファイルを生成する:
  - models/item/card_xxx.json
  - lang(ja_jp/en_us) の item 表示名キー
  - ModItems.java に貼り付ける登録行（標準出力に出力）
効果ロジックは Java(ProduceSenseCards) 側で手書きする。
"""
import json
import os

MODEL_DIR = "src/main/resources/assets/gakumas_produce/models/item"
LANG_DIR = "src/main/resources/assets/gakumas_produce/lang"

# id -> (日本語名, 英語名)
CARDS = [
    ("card_challenge", "挑戦", "Challenge"),
    ("card_trial_error", "試行錯誤", "Trial and Error"),
    ("card_gaze_basic", "視線の基本", "Basic Gaze"),
    ("card_thinking_basic", "思考の基本", "Basic Thinking"),
    ("card_composure_basic", "落ち着きの基本", "Basic Composure"),
    ("card_timing_basic", "タイミングの基本", "Basic Timing"),
    ("card_light_steps", "軽い足取り", "Light Steps"),
    ("card_charm", "愛嬌", "Charm"),
    ("card_warmup", "準備運動", "Warm-up"),
    ("card_fan_service", "ファンサ", "Fan Service"),
    ("card_momentum", "勢い任せ", "Momentum"),
    ("card_high_touch", "ハイタッチ", "High Touch"),
    ("card_talk_time", "トークタイム", "Talk Time"),
    ("card_course_correction", "軌道修正", "Course Correction"),
    ("card_pump_up", "パンプアップ", "Pump Up"),
    ("card_pacing", "ペース配分", "Pacing"),
    ("card_balance_sense", "バランス感覚", "Sense of Balance"),
    ("card_optimistic", "楽観的", "Optimistic"),
    ("card_deep_breath", "深呼吸", "Deep Breath"),
    ("card_one_breath", "ひと呼吸", "One Breath"),
    ("card_decided_pose", "決めポーズ", "Signature Pose"),
    ("card_adlib", "アドリブ", "Ad-lib"),
    ("card_passion_turn", "情熱ターン", "Passion Turn"),
    ("card_leap", "飛躍", "Leap"),
    ("card_blessing", "祝福", "Blessing"),
    ("card_start_dash", "スタートダッシュ", "Start Dash"),
    ("card_stand_play", "スタンドプレー", "Standout Play"),
    ("card_position_check", "立ち位置チェック", "Position Check"),
    ("card_unstoppable", "破竹の勢い", "Unstoppable"),
    ("card_keen_eye", "眼力", "Keen Eye"),
    ("card_big_cheer", "大声援", "Big Cheer"),
    ("card_power_of_wish", "願いの力", "Power of Wish"),
    ("card_starting_signal", "始まりの合図", "Starting Signal"),
    ("card_grit", "意地", "Grit"),
    ("card_path_to_success", "成功への道筋", "Path to Success"),
    ("card_spotlight", "スポットライト", "Spotlight"),
    ("card_one_shot", "一発勝負", "One Shot"),
    ("card_thrilling", "スリリング", "Thrilling"),
    ("card_fearless", "大胆不敵", "Fearless"),
    ("card_mental_unity", "精神統一", "Mental Focus"),
    ("card_buzzword", "バズワード", "Buzzword"),
    ("card_fulfillment", "成就", "Fulfillment"),
    ("card_charming_performance", "魅惑のパフォーマンス", "Charming Performance"),
    ("card_supreme_entertainment", "至高のエンタメ", "Supreme Entertainment"),
    ("card_awakening", "覚醒", "Awakening"),
    ("card_limelight", "脚光", "Limelight"),
    ("card_hot_topic", "話題沸騰", "Hot Topic"),
    ("card_national_idol", "国民的アイドル", "National Idol"),
    ("card_endless_applause", "鳴り止まない拍手", "Endless Applause"),
    ("card_natural_talent", "天賦の才", "Natural Talent"),
]


def write_models():
    os.makedirs(MODEL_DIR, exist_ok=True)
    for cid, _, _ in CARDS:
        model = {
            "parent": "item/generated",
            "textures": {"layer0": f"gakumas_produce:item/{cid}"},
        }
        with open(f"{MODEL_DIR}/{cid}.json", "w", encoding="utf-8") as f:
            json.dump(model, f, ensure_ascii=False, indent=2)
    print(f"wrote {len(CARDS)} model jsons")


def update_lang(fname, idx):
    path = f"{LANG_DIR}/{fname}"
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    for cid, jp, en in CARDS:
        data[f"item.gakumas_produce.{cid}"] = jp if idx == 0 else en
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"updated {fname} (+{len(CARDS)} keys)")


def print_moditems():
    print("\n// ==== 貼り付け用: ModItems 登録行 ====")
    for cid, jp, _ in CARDS:
        const = cid.upper()
        print(f'    public static final RegistryObject<Item> {const} = ITEMS.register("{cid}",')
        print('            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));')


if __name__ == "__main__":
    write_models()
    update_lang("ja_jp.json", 0)
    update_lang("en_us.json", 1)
    print_moditems()
