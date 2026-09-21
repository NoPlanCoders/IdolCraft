#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
フリー／ロジック／アノマリー新規カードの定型ファイルを生成する:
  - models/item/card_xxx.json
  - lang(ja_jp/en_us) の item 表示名キー
  - ModItems.java に貼り付ける登録行（標準出力）
  - CardCatalog.java に貼り付けるレア度登録行（標準出力）
効果ロジックは Java(ProduceFreeCards/ProduceLogicCards/ProduceAnomalyCards)側で手書き済み。
"""
import json
import os

MODEL_DIR = "src/main/resources/assets/idolcraft/models/item"
LANG_DIR = "src/main/resources/assets/idolcraft/lang"

# id -> (日本語名, 英語名, レア度 or None=トラブルカード扱い)
# レア度: "WHITE" | "SILVER" | "GOLD" | "RAINBOW" | None
CARDS = [
    # ---- トラブルカード ----
    ("card_drowsiness", "眠気", "Drowsiness", None),

    # ---- フリープラン ----
    ("card_free_fired_up", "気合十分！", "Fired Up!", "SILVER"),
    ("card_free_first_step", "ファーストステップ", "First Step", "SILVER"),
    ("card_free_bright_future", "前途洋々", "Bright Future", "GOLD"),
    ("card_free_idol_declaration", "アイドル宣言", "Idol Declaration", "GOLD"),
    ("card_free_high_tension", "ハイテンション", "High Tension", "GOLD"),
    ("card_free_tv_appearance", "テレビ出演", "TV Appearance", "RAINBOW"),
    ("card_free_dream_to_fulfill", "かなえたい夢", "A Dream to Fulfill", "RAINBOW"),
    ("card_free_idol_spirit", "アイドル魂", "Idol Spirit", "RAINBOW"),
    ("card_free_fresh_start", "仕切り直し", "Fresh Start", "RAINBOW"),

    # ---- ロジックプラン R(銀) ----
    ("card_logic_good_morning", "今日もおはよう", "Good Morning Again", "SILVER"),
    ("card_logic_easy_chat", "ゆるふわおしゃべり", "Easy Chat", "SILVER"),
    ("card_logic_just_a_bit_more", "もう少しだけ", "Just a Bit More", "SILVER"),
    ("card_logic_clapping", "手拍子", "Clapping", "SILVER"),
    ("card_logic_energetic_greeting", "元気な挨拶", "Energetic Greeting", "SILVER"),
    ("card_logic_lucky_charm", "おまもりミラクル", "Lucky Charm", "SILVER"),
    ("card_logic_reckless", "がむしゃら", "Reckless", "SILVER"),
    ("card_logic_daydreaming", "デイドリーミング", "Daydreaming", "SILVER"),
    ("card_logic_restart", "リスタート", "Restart", "SILVER"),
    ("card_logic_ei_ei_oh", "えいえいおー", "Ei Ei Oh", "SILVER"),
    ("card_logic_rhythmical", "リズミカル", "Rhythmical", "SILVER"),
    ("card_logic_fond_memory_laugh", "思い出し笑い", "Fond Memory Laugh", "SILVER"),
    ("card_logic_pastel_mood", "パステル気分", "Pastel Mood", "SILVER"),
    ("card_logic_encouragement", "励まし", "Encouragement", "SILVER"),
    ("card_logic_happy_charm", "幸せのおまじない", "Happy Charm", "SILVER"),
    ("card_logic_makeover", "イメチェン", "Makeover", "SILVER"),

    # ---- ロジックプラン SR(金) ----
    ("card_logic_lovely_wink", "ラブリーウィンク", "Lovely Wink", "GOLD"),
    ("card_logic_words_of_thanks", "ありがとうの言葉", "Words of Thanks", "GOLD"),
    ("card_logic_heart_signal", "ハートの合図", "Heart Signal", "GOLD"),
    ("card_logic_sparkle", "キラメキ", "Sparkle", "GOLD"),
    ("card_logic_everyone_loves", "みんな大好き", "Everyone Loves It", "GOLD"),
    ("card_logic_glitter_confetti", "きらきら紙吹雪", "Glitter Confetti", "GOLD"),
    ("card_logic_overflowing_memories", "あふれる思い出", "Overflowing Memories", "GOLD"),
    ("card_logic_bonding", "ふれあい", "Bonding", "GOLD"),
    ("card_logic_happy_time", "幸せな時間", "Happy Time", "GOLD"),
    ("card_logic_fancy_charm", "ファンシーチャーム", "Fancy Charm", "GOLD"),
    ("card_logic_cant_stop_excitement", "ワクワクが止まらない", "Can't Stop the Excitement", "GOLD"),
    ("card_logic_night_before", "本番前夜", "The Night Before", "GOLD"),
    ("card_logic_sunbathing", "ひなたぼっこ", "Sunbathing", "GOLD"),
    ("card_logic_image_training", "イメトレ", "Image Training", "GOLD"),
    ("card_logic_full_of_motivation", "やる気は満点", "Full of Motivation", "GOLD"),
    ("card_logic_dreamy_mood", "ゆめみごこち", "Dreamy Mood", "GOLD"),
    ("card_logic_unstoppable_feelings", "止められない想い", "Unstoppable Feelings", "GOLD"),
    ("card_logic_maiden_heart", "オトメゴコロ", "Maiden Heart", "GOLD"),
    ("card_logic_adventurous_spirit", "冒険心", "Adventurous Spirit", "GOLD"),
    ("card_logic_capricious_heart", "気まぐれハート", "Capricious Heart", "GOLD"),
    ("card_logic_growing_pains", "成長痛", "Growing Pains", "GOLD"),

    # ---- ロジックプラン SSR(虹) ----
    ("card_logic_200_smile", "200%スマイル", "200% Smile", "RAINBOW"),
    ("card_logic_blooming", "開花", "Blooming", "RAINBOW"),
    ("card_logic_reach_you", "届いて!", "Reach You!", "RAINBOW"),
    ("card_logic_shine_for_you", "輝くキミへ", "Shine For You", "RAINBOW"),
    ("card_logic_that_promise", "あのときの約束", "That Promise", "RAINBOW"),
    ("card_logic_miracle_magic", "キセキの魔法", "Miracle Magic", "RAINBOW"),
    ("card_logic_growth_spurt_magic", "せのびの魔法", "Growth Spurt Magic", "RAINBOW"),
    ("card_logic_perfect_pose_face", "びしっとキメ顔", "Perfect Pose Face", "RAINBOW"),
    ("card_logic_im_the_star", "私がスター", "I'm the Star", "RAINBOW"),
    ("card_logic_stardust_sensation", "星屑センセーション", "Stardust Sensation", "RAINBOW"),
    ("card_logic_notebook_resolve", "ノートの端の決意", "Notebook Resolve", "RAINBOW"),
    ("card_logic_handwritten_message", "手書きのメッセージ", "Handwritten Message", "RAINBOW"),
    ("card_logic_heart_flutter", "トキメキ", "Heart Flutter", "RAINBOW"),
    ("card_logic_rainbow_dreamer", "虹色ドリーマー", "Rainbow Dreamer", "RAINBOW"),
    ("card_logic_dreamy_lip", "夢色リップ", "Dreamy Lip", "RAINBOW"),

    # ---- アノマリープラン R(銀) ----
    ("card_anomaly_just_appeal", "ジャストアピール", "Just Appeal", "SILVER"),
    ("card_anomaly_starlight", "スターライト", "Starlight", "SILVER"),
    ("card_anomaly_one_step", "一歩", "One Step", "SILVER"),
    ("card_anomaly_lucky", "ラッキー♪", "Lucky", "SILVER"),
    ("card_anomaly_steady_stack", "積み重ね", "Steady Stack", "SILVER"),
    ("card_anomaly_all_out", "精一杯", "All Out", "SILVER"),
    ("card_anomaly_turnaround", "形成逆転", "Turnaround", "SILVER"),
    ("card_anomaly_nonstop", "ノンストップ", "Nonstop", "SILVER"),
    ("card_anomaly_hustle", "ハッスル", "Hustle", "SILVER"),
    ("card_anomaly_happy_note", "ハッピー♪", "Happy Note", "SILVER"),
    ("card_anomaly_happy_accident", "嬉しい誤算", "Happy Accident", "SILVER"),
    ("card_anomaly_tearful_memory", "涙の思い出", "Tearful Memory", "SILVER"),
    ("card_anomaly_setting", "セッティング", "Setting", "SILVER"),
    ("card_anomaly_comeback", "巻き返し", "Comeback", "SILVER"),

    # ---- アノマリープラン SR(金) ----
    ("card_anomaly_ready_set", "せーのっ!", "Ready, Set!", "GOLD"),
    ("card_anomaly_accelerando", "アッチェレランド", "Accelerando", "GOLD"),
    ("card_anomaly_bursting_passion", "はじけるパッション", "Bursting Passion", "GOLD"),
    ("card_anomaly_sweat_and_growth", "汗と成長", "Sweat and Growth", "GOLD"),
    ("card_anomaly_first_impression", "第一印象", "First Impression", "GOLD"),
    ("card_anomaly_opening_act", "オープニングアクト", "Opening Act", "GOLD"),
    ("card_anomaly_starting_smile", "始まりの笑顔", "Starting Smile", "GOLD"),
    ("card_anomaly_trend_leader", "トレンドリーダー", "Trend Leader", "GOLD"),
    ("card_anomaly_ideal_tempo", "理想のテンポ", "Ideal Tempo", "GOLD"),
    ("card_anomaly_training_result", "トレーニングの成果", "Training Result", "GOLD"),
    ("card_anomaly_overdrive", "オーバードライブ", "Overdrive", "GOLD"),
    ("card_anomaly_andante", "アンダンテ", "Andante", "GOLD"),
    ("card_anomaly_potential", "潜在能力", "Potential", "GOLD"),
    ("card_anomaly_countdown", "カウントダウン", "Countdown", "GOLD"),
    ("card_anomaly_motivation_boost", "モチベ", "Motivation", "GOLD"),
    ("card_anomaly_pride", "プライド", "Pride", "GOLD"),
    ("card_anomaly_hype_master", "盛り上げ上手", "Hype Master", "GOLD"),
    ("card_anomaly_influencer", "インフルエンサー", "Influencer", "GOLD"),
    ("card_anomaly_patience", "忍耐力", "Patience", "GOLD"),
    ("card_anomaly_mutual_improvement", "切磋琢磨", "Mutual Improvement", "GOLD"),
    ("card_anomaly_full_throttle", "フルスロットル", "Full Throttle", "GOLD"),
    ("card_anomaly_risky_chance", "リスキーチャンス", "Risky Chance", "GOLD"),
    ("card_anomaly_toughness", "タフネス", "Toughness", "GOLD"),
    ("card_anomaly_sense_of_achievement", "達成感", "Sense of Achievement", "GOLD"),

    # ---- アノマリープラン SSR(虹) ----
    ("card_anomaly_take_flight", "翔び立て", "Take Flight", "RAINBOW"),
    ("card_anomaly_total_art", "総合芸術", "Total Art", "RAINBOW"),
    ("card_anomaly_mind_body_spirit", "心・技・体", "Mind, Body, Spirit", "RAINBOW"),
    ("card_anomaly_shine", "輝け!", "Shine!", "RAINBOW"),
    ("card_anomaly_climax", "クライマックス", "Climax", "RAINBOW"),
    ("card_anomaly_all_in", "全身全霊", "All In", "RAINBOW"),
    ("card_anomaly_entertainer", "エンターテイナー", "Entertainer", "RAINBOW"),
    ("card_anomaly_soar", "羽ばたけ!", "Soar!", "RAINBOW"),
    ("card_anomaly_becoming_idol", "アイドルになります", "Becoming an Idol", "RAINBOW"),
    ("card_anomaly_single_minded", "一心不乱", "Single-Minded", "RAINBOW"),
    ("card_anomaly_to_the_top", "頂点へ", "To the Top", "RAINBOW"),
    ("card_anomaly_resolve", "覚悟", "Resolve", "RAINBOW"),
    ("card_anomaly_true_potential", "本領発揮", "True Potential", "RAINBOW"),
]


def write_models():
    os.makedirs(MODEL_DIR, exist_ok=True)
    for cid, _, _, _ in CARDS:
        model = {
            "parent": "item/generated",
            "textures": {"layer0": f"idolcraft:item/{cid}"},
        }
        with open(f"{MODEL_DIR}/{cid}.json", "w", encoding="utf-8") as f:
            json.dump(model, f, ensure_ascii=False, indent=2)
    print(f"wrote {len(CARDS)} model jsons")


def update_lang(fname, idx):
    path = f"{LANG_DIR}/{fname}"
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    for cid, jp, en, _ in CARDS:
        data[f"item.idolcraft.{cid}"] = jp if idx == 0 else en
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"updated {fname} (+{len(CARDS)} keys)")


def print_moditems():
    print("\n// ==== 貼り付け用: ModItems 登録行 ====")
    for cid, jp, _, _ in CARDS:
        const = cid.upper()
        print(f'    public static final RegistryObject<Item> {const} = ITEMS.register("{cid}",')
        print('            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));')


def print_cardcatalog():
    print("\n// ==== 貼り付け用: CardCatalog レア度登録 ====")
    for rarity in ("SILVER", "GOLD", "RAINBOW"):
        ids = [cid for cid, _, _, r in CARDS if r == rarity]
        if not ids:
            continue
        print(f"        // {rarity}")
        for i in range(0, len(ids), 4):
            chunk = ids[i:i + 4]
            body = ", ".join(f'"{c}"' for c in chunk)
            print(f'        for (String p : new String[]{{{body}}}) put(p, CardRarity.{rarity});')


if __name__ == "__main__":
    write_models()
    update_lang("ja_jp.json", 0)
    update_lang("en_us.json", 1)
    print_moditems()
    print_cardcatalog()
