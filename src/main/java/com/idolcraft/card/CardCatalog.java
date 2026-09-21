package com.idolcraft.card;

import com.idolcraft.IdolCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * カードID → レア度の対応表。レア度別カードパックの抽選に使う。
 * 本家学マス wiki のレア度（白/銀/金/虹）に準拠。
 */
public final class CardCatalog {

    private CardCatalog() {}

    private static final Map<ResourceLocation, CardRarity> RARITY = new HashMap<>();

    private static void put(String path, CardRarity r) {
        RARITY.put(new ResourceLocation(IdolCraft.MOD_ID, path), r);
    }

    static {
        // ── 初期17カード ──
        put("card_appeal_basic", CardRarity.WHITE);
        put("card_expression_basic", CardRarity.WHITE);
        put("card_behavior_basic", CardRarity.WHITE);
        put("card_facial_basic", CardRarity.WHITE);
        put("card_staging_basic", CardRarity.WHITE);
        put("card_step_basic", CardRarity.WHITE);
        put("card_performance_basic", CardRarity.WHITE);
        put("card_reaction_basic", CardRarity.WHITE);
        put("card_pose_basic", CardRarity.WHITE);
        put("card_excite", CardRarity.SILVER);
        put("card_quiet_will", CardRarity.GOLD);
        put("card_direction_plan", CardRarity.GOLD);
        put("card_shofu_hiko", CardRarity.GOLD);
        put("card_existence", CardRarity.GOLD);
        put("card_fascination", CardRarity.RAINBOW);
        put("card_innocence", CardRarity.RAINBOW);
        put("card_call_response", CardRarity.RAINBOW);

        // ── センス追加50カード ──
        // 白
        for (String p : new String[]{"card_challenge", "card_trial_error", "card_gaze_basic",
                "card_thinking_basic", "card_composure_basic", "card_timing_basic"}) put(p, CardRarity.WHITE);
        // 銀
        for (String p : new String[]{"card_light_steps", "card_charm", "card_warmup", "card_fan_service",
                "card_momentum", "card_high_touch", "card_talk_time", "card_course_correction", "card_pump_up",
                "card_pacing", "card_balance_sense", "card_optimistic", "card_deep_breath", "card_one_breath"}) put(p, CardRarity.SILVER);
        // 金
        for (String p : new String[]{"card_decided_pose", "card_adlib", "card_passion_turn", "card_leap",
                "card_blessing", "card_start_dash", "card_stand_play", "card_position_check", "card_unstoppable",
                "card_keen_eye", "card_big_cheer", "card_power_of_wish", "card_starting_signal", "card_grit",
                "card_path_to_success", "card_spotlight", "card_one_shot", "card_thrilling", "card_fearless",
                "card_mental_unity"}) put(p, CardRarity.GOLD);
        // 虹
        for (String p : new String[]{"card_buzzword", "card_fulfillment", "card_charming_performance",
                "card_supreme_entertainment", "card_awakening", "card_limelight", "card_hot_topic",
                "card_national_idol", "card_endless_applause", "card_natural_talent"}) put(p, CardRarity.RAINBOW);

        // ── フリー／ロジック／アノマリー追加カード（issueコメント添付 F/R/A json 準拠） ──
        // SILVER
        for (String p : new String[]{"card_free_fired_up", "card_free_first_step", "card_logic_good_morning", "card_logic_easy_chat"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_logic_just_a_bit_more", "card_logic_clapping", "card_logic_energetic_greeting", "card_logic_lucky_charm"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_logic_reckless", "card_logic_daydreaming", "card_logic_restart", "card_logic_ei_ei_oh"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_logic_rhythmical", "card_logic_fond_memory_laugh", "card_logic_pastel_mood", "card_logic_encouragement"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_logic_happy_charm", "card_logic_makeover", "card_anomaly_just_appeal", "card_anomaly_starlight"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_anomaly_one_step", "card_anomaly_lucky", "card_anomaly_steady_stack", "card_anomaly_all_out"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_anomaly_turnaround", "card_anomaly_nonstop", "card_anomaly_hustle", "card_anomaly_happy_note"}) put(p, CardRarity.SILVER);
        for (String p : new String[]{"card_anomaly_happy_accident", "card_anomaly_tearful_memory", "card_anomaly_setting", "card_anomaly_comeback"}) put(p, CardRarity.SILVER);
        // GOLD
        for (String p : new String[]{"card_free_bright_future", "card_free_idol_declaration", "card_free_high_tension", "card_logic_lovely_wink"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_logic_words_of_thanks", "card_logic_heart_signal", "card_logic_sparkle", "card_logic_everyone_loves"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_logic_glitter_confetti", "card_logic_overflowing_memories", "card_logic_bonding", "card_logic_happy_time"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_logic_fancy_charm", "card_logic_cant_stop_excitement", "card_logic_night_before", "card_logic_sunbathing"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_logic_image_training", "card_logic_full_of_motivation", "card_logic_dreamy_mood", "card_logic_unstoppable_feelings"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_logic_maiden_heart", "card_logic_adventurous_spirit", "card_logic_capricious_heart", "card_logic_growing_pains"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_ready_set", "card_anomaly_accelerando", "card_anomaly_bursting_passion", "card_anomaly_sweat_and_growth"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_first_impression", "card_anomaly_opening_act", "card_anomaly_starting_smile", "card_anomaly_trend_leader"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_ideal_tempo", "card_anomaly_training_result", "card_anomaly_overdrive", "card_anomaly_andante"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_potential", "card_anomaly_countdown", "card_anomaly_motivation_boost", "card_anomaly_pride"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_hype_master", "card_anomaly_influencer", "card_anomaly_patience", "card_anomaly_mutual_improvement"}) put(p, CardRarity.GOLD);
        for (String p : new String[]{"card_anomaly_full_throttle", "card_anomaly_risky_chance", "card_anomaly_toughness", "card_anomaly_sense_of_achievement"}) put(p, CardRarity.GOLD);
        // RAINBOW
        for (String p : new String[]{"card_free_tv_appearance", "card_free_dream_to_fulfill", "card_free_idol_spirit", "card_free_fresh_start"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_logic_200_smile", "card_logic_blooming", "card_logic_reach_you", "card_logic_shine_for_you"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_logic_that_promise", "card_logic_miracle_magic", "card_logic_growth_spurt_magic", "card_logic_perfect_pose_face"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_logic_im_the_star", "card_logic_stardust_sensation", "card_logic_notebook_resolve", "card_logic_handwritten_message"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_logic_heart_flutter", "card_logic_rainbow_dreamer", "card_logic_dreamy_lip", "card_anomaly_take_flight"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_anomaly_total_art", "card_anomaly_mind_body_spirit", "card_anomaly_shine", "card_anomaly_climax"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_anomaly_all_in", "card_anomaly_entertainer", "card_anomaly_soar", "card_anomaly_becoming_idol"}) put(p, CardRarity.RAINBOW);
        for (String p : new String[]{"card_anomaly_single_minded", "card_anomaly_to_the_top", "card_anomaly_resolve", "card_anomaly_true_potential"}) put(p, CardRarity.RAINBOW);
    }

    /** レア度不明のカードは白扱いにする（安全側） */
    public static CardRarity rarityOf(ResourceLocation id) {
        return RARITY.getOrDefault(id, CardRarity.WHITE);
    }

    /** 指定レア度のカードIDを全て返す */
    public static List<ResourceLocation> cardsOfRarity(CardRarity rarity) {
        List<ResourceLocation> out = new ArrayList<>();
        for (CardDefinition def : CardRegistry.all()) {
            if (def.isTrouble()) continue; // トラブルカードはパック抽選の対象外
            if (rarityOf(def.getId()) == rarity) out.add(def.getId());
        }
        return out;
    }

    /** 指定レア度からランダムに1枚選ぶ。該当が無ければ null。 */
    public static ResourceLocation randomOfRarity(CardRarity rarity, RandomSource random) {
        List<ResourceLocation> pool = cardsOfRarity(rarity);
        if (pool.isEmpty()) return null;
        return pool.get(random.nextInt(pool.size()));
    }
}

