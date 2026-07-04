package com.gakumas.produce.card;

import com.gakumas.produce.GakumasProduceMod;
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
        RARITY.put(new ResourceLocation(GakumasProduceMod.MOD_ID, path), r);
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
    }

    /** レア度不明のカードは白扱いにする（安全側） */
    public static CardRarity rarityOf(ResourceLocation id) {
        return RARITY.getOrDefault(id, CardRarity.WHITE);
    }

    /** 指定レア度のカードIDを全て返す */
    public static List<ResourceLocation> cardsOfRarity(CardRarity rarity) {
        List<ResourceLocation> out = new ArrayList<>();
        for (CardDefinition def : CardRegistry.all()) {
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
