package com.gakumas.produce.card.impl;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.buff.BuffState;
import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.capability.IDeckData;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.card.CardType;
import com.gakumas.produce.util.GenkiHelper;
import com.gakumas.produce.util.ScoreMath;
import com.gakumas.produce.util.TargetingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * センスプランの追加スキルカード（本家学マス wiki のスキルカード一覧に準拠）。
 *
 * 効果は集中/好調/絶好調/元気/追加使用/ドロー/消費体力減少/2回発動/継続パラメータで再現している。
 * 本家の「消費体力減少」「継続火力」「SSR調達」等、本Modに厳密な対応概念が無いものは近似・簡略化しており、
 * 各カードのコメントに簡単に注記している。解放PレベルはwikiのPLvに一致させている。
 */
public final class ProduceSenseCards {

    private ProduceSenseCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, path);
    }

    // ── 効果ヘルパー ──
    private static void dmg(ServerPlayer p, IDeckData d, int base) {
        LivingEntity t = TargetingHelper.getLookTarget(p);
        if (t != null) t.hurt(p.level().damageSources().magic(), ScoreMath.calculateDamage(base, d.getBuffState()));
    }
    private static void good(IDeckData d, int turns) { d.getBuffState().addGoodCondition(turns * DeckService.TICKS_PER_TURN, turns); }
    private static void great(IDeckData d, int turns) { d.getBuffState().addGreatCondition(turns * DeckService.TICKS_PER_TURN); }

    private static CardDefinition.Builder b(String path, String name, CardType type) {
        return CardDefinition.builder(id(path), name, type);
    }

    public static void registerAll() {

        // ============ 白（N） ============
        CardRegistry.register(b("card_challenge", "挑戦", CardType.NORMAL)
                .description("ダメージ+25").hpCost(7)
                .effect((p, d) -> dmg(p, d, 25)).build());

        CardRegistry.register(b("card_trial_error", "試行錯誤", CardType.NORMAL)
                .description("ダメージ+8を2回").hpCost(7)
                .effect((p, d) -> { dmg(p, d, 8); dmg(p, d, 8); }).build());

        CardRegistry.register(b("card_gaze_basic", "視線の基本", CardType.NORMAL)
                .description("元気+5、好調2ターン付与").hpCost(2)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 5f); good(d, 2); }).build());

        CardRegistry.register(b("card_thinking_basic", "思考の基本", CardType.NORMAL)
                .description("好調2ターン付与、集中+2").hpCost(4)
                .effect((p, d) -> { good(d, 2); d.getBuffState().addFocus(2); }).build());

        CardRegistry.register(b("card_composure_basic", "落ち着きの基本", CardType.NORMAL)
                .description("元気+5、集中+2").hpCost(2)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 5f); d.getBuffState().addFocus(2); }).build());

        CardRegistry.register(b("card_timing_basic", "タイミングの基本", CardType.NORMAL)
                .description("元気+5、絶好調1ターン付与").hpCost(0)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 5f); great(d, 1); }).build());

        // ============ 銀（SR） ============
        CardRegistry.register(b("card_light_steps", "軽い足取り", CardType.NORMAL)
                .description("ダメージ+6、好調2ターン付与").hpCost(4)
                .effect((p, d) -> { dmg(p, d, 6); good(d, 2); }).build());

        CardRegistry.register(b("card_charm", "愛嬌", CardType.NORMAL)
                .description("ダメージ+13").hpCost(4)
                .effect((p, d) -> dmg(p, d, 13)).build());

        CardRegistry.register(b("card_warmup", "準備運動", CardType.NORMAL)
                .description("ダメージ+6、集中+2").hpCost(4)
                .effect((p, d) -> { dmg(p, d, 6); d.getBuffState().addFocus(2); }).build());

        CardRegistry.register(b("card_fan_service", "ファンサ", CardType.NORMAL)
                .description("ダメージ+10").hpCost(2).requiredPLevel(2)
                .effect((p, d) -> dmg(p, d, 10)).build());

        CardRegistry.register(b("card_momentum", "勢い任せ", CardType.NORMAL)
                .description("ダメージ+6、好調中なら集中+3").hpCost(4).requiredPLevel(9)
                .effect((p, d) -> { dmg(p, d, 6); if (d.getBuffState().isGoodConditionActive()) d.getBuffState().addFocus(3); }).build());

        CardRegistry.register(b("card_high_touch", "ハイタッチ", CardType.NORMAL)
                .description("ダメージ+17（集中効果1.5倍）").hpCost(4).requiredPLevel(13)
                .effect((p, d) -> dmg(p, d, 17)).build());

        CardRegistry.register(b("card_talk_time", "トークタイム", CardType.NORMAL)
                .description("好調中ならダメージ+27、そうでなければ+10").hpCost(6).requiredPLevel(14)
                .effect((p, d) -> dmg(p, d, d.getBuffState().isGoodConditionActive() ? 27 : 10)).build());

        CardRegistry.register(b("card_course_correction", "軌道修正", CardType.NORMAL)
                .description("ダメージ+6、集中+2").hpCost(4)
                .effect((p, d) -> { dmg(p, d, 6); d.getBuffState().addFocus(2); }).build());

        CardRegistry.register(b("card_pump_up", "パンプアップ", CardType.NORMAL)
                .description("ダメージ+6、好調2ターン付与").hpCost(4)
                .effect((p, d) -> { dmg(p, d, 6); good(d, 2); }).build());

        CardRegistry.register(b("card_pacing", "ペース配分", CardType.NORMAL)
                .description("ダメージ+3、集中+3、好調3ターン付与").hpCost(4).requiredPLevel(67)
                .effect((p, d) -> { dmg(p, d, 3); d.getBuffState().addFocus(3); good(d, 3); }).build());

        CardRegistry.register(b("card_balance_sense", "バランス感覚", CardType.NORMAL)
                .description("集中+3").hpCost(2)
                .effect((p, d) -> d.getBuffState().addFocus(3)).build());

        CardRegistry.register(b("card_optimistic", "楽観的", CardType.NORMAL)
                .description("好調3ターン付与、集中があればさらに集中+1").hpCost(2).requiredPLevel(4)
                .effect((p, d) -> { good(d, 3); if (d.getBuffState().getFocusStacks() > 0) d.getBuffState().addFocus(1); }).build());

        CardRegistry.register(b("card_deep_breath", "深呼吸", CardType.NORMAL)
                .description("集中+2、集中3以上なら好調3ターン付与").hpCost(3).requiredPLevel(19)
                .effect((p, d) -> { d.getBuffState().addFocus(2); if (d.getBuffState().getFocusStacks() >= 3) good(d, 3); }).build());

        CardRegistry.register(b("card_one_breath", "ひと呼吸", CardType.NORMAL)
                .description("好調3ターン付与、集中+4").hpCost(7).requiredPLevel(47)
                .effect((p, d) -> { good(d, 3); d.getBuffState().addFocus(4); }).build());

        // ============ 金（SSR） ============
        CardRegistry.register(b("card_decided_pose", "決めポーズ", CardType.NORMAL)
                .description("ダメージ+18").hpCost(3)
                .effect((p, d) -> dmg(p, d, 18)).build());

        CardRegistry.register(b("card_adlib", "アドリブ", CardType.NORMAL)
                .description("ダメージ+5、好調3ターン付与").hpCost(4)
                .effect((p, d) -> { dmg(p, d, 5); good(d, 3); }).build());

        CardRegistry.register(b("card_passion_turn", "情熱ターン", CardType.NORMAL)
                .description("ダメージ+11、集中+3").hpCost(6)
                .effect((p, d) -> { dmg(p, d, 11); d.getBuffState().addFocus(3); }).build());

        CardRegistry.register(b("card_leap", "飛躍", CardType.NORMAL)
                .description("ダメージ+13、集中3以上ならさらに+15").hpCost(5).requiredPLevel(6)
                .effect((p, d) -> dmg(p, d, d.getBuffState().getFocusStacks() >= 3 ? 28 : 13)).build());

        CardRegistry.register(b("card_blessing", "祝福", CardType.NORMAL)
                .description("ダメージ+26、好調1ターン付与").hpCost(4).requiredPLevel(7)
                .effect((p, d) -> { dmg(p, d, 26); good(d, 1); }).build());

        CardRegistry.register(b("card_start_dash", "スタートダッシュ", CardType.NORMAL)
                .description("ダメージ+30、元気+10").hpCost(5).requiredPLevel(22)
                .effect((p, d) -> { dmg(p, d, 30); GenkiHelper.addGenki(p, 10f); }).build());

        CardRegistry.register(b("card_stand_play", "スタンドプレー", CardType.NORMAL)
                .description("ダメージ+12、元気+7、集中+5").hpCost(5).requiredPLevel(24)
                .effect((p, d) -> { dmg(p, d, 12); GenkiHelper.addGenki(p, 7f); d.getBuffState().addFocus(5); }).build());

        CardRegistry.register(b("card_position_check", "立ち位置チェック", CardType.ONCE_PER_LESSON)
                .description("集中3消費、ダメージ+30、元気+15").hpCost(0).requiredPLevel(51)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((p, d) -> { d.getBuffState().addFocus(-3); dmg(p, d, 30); GenkiHelper.addGenki(p, 15f); }).build());

        CardRegistry.register(b("card_unstoppable", "破竹の勢い", CardType.NORMAL)
                .description("集中+5、以後ターン終了毎にダメージ+3（継続）").hpCost(2).requiredPLevel(69)
                .effect((p, d) -> { d.getBuffState().addFocus(5); d.getBuffState().addParamPerTurn(3); }).build());

        CardRegistry.register(b("card_keen_eye", "眼力", CardType.NORMAL)
                .description("元気+6、集中+3").hpCost(3)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 6f); d.getBuffState().addFocus(3); }).build());

        CardRegistry.register(b("card_big_cheer", "大声援", CardType.NORMAL)
                .description("元気+6、好調3ターン付与").hpCost(3)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 6f); good(d, 3); }).build());

        CardRegistry.register(b("card_power_of_wish", "願いの力", CardType.NORMAL)
                .description("集中+2、以後ターン終了時に集中3以上なら集中+2（継続）").hpCost(4).requiredPLevel(18)
                .effect((p, d) -> {
                    d.getBuffState().addFocus(2);
                    BuffState buff = d.getBuffState();
                    buff.setCustomCounter("focus_per_turn_stacks", buff.getCustomCounter("focus_per_turn_stacks") + 1);
                }).build());

        CardRegistry.register(b("card_starting_signal", "始まりの合図", CardType.NORMAL)
                .description("好調5ターン付与").hpCost(3).requiredPLevel(28)
                .effect((p, d) -> good(d, 5)).build());

        CardRegistry.register(b("card_grit", "意地", CardType.NORMAL)
                .description("元気+3、集中+4").hpCost(2).requiredPLevel(29)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 3f); d.getBuffState().addFocus(4); }).build());

        CardRegistry.register(b("card_path_to_success", "成功への道筋", CardType.NORMAL)
                .description("好調消費、元気+5、集中+7").hpCost(0).requiredPLevel(36)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((p, d) -> { GenkiHelper.addGenki(p, 5f); d.getBuffState().addFocus(7); }).build());

        CardRegistry.register(b("card_spotlight", "スポットライト", CardType.ONCE_PER_LESSON)
                .description("元気+7、好調5ターン付与、カードを1枚ドロー").hpCost(3).requiredPLevel(49)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 7f); good(d, 5); d.getBuffState().addPendingDraw(1); }).build());

        CardRegistry.register(b("card_one_shot", "一発勝負", CardType.ONCE_PER_LESSON)
                .description("集中+7、追加でもう1枚使用可、カードを1枚ドロー").hpCost(5).requiredPLevel(53)
                .effect((p, d) -> { d.getBuffState().addFocus(7); d.getBuffState().addBonusAction(1); d.getBuffState().addPendingDraw(1); }).build());

        CardRegistry.register(b("card_thrilling", "スリリング", CardType.NORMAL)
                .description("好調5ターン付与、追加でもう1枚使用可").hpCost(2).requiredPLevel(54)
                .effect((p, d) -> { good(d, 5); d.getBuffState().addBonusAction(1); }).build());

        CardRegistry.register(b("card_fearless", "大胆不敵", CardType.NORMAL)
                .description("好調3ターン付与、集中+5").hpCost(6).requiredPLevel(56)
                .effect((p, d) -> { good(d, 3); d.getBuffState().addFocus(5); }).build());

        CardRegistry.register(b("card_mental_unity", "精神統一", CardType.ONCE_PER_LESSON)
                .description("元気+6、集中+5、カードを1枚ドロー").hpCost(0).requiredPLevel(74)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 6f); d.getBuffState().addFocus(5); d.getBuffState().addPendingDraw(1); }).build());

        // ============ 虹（UR） ============
        CardRegistry.register(b("card_buzzword", "バズワード", CardType.ONCE_PER_LESSON)
                .description("好調中ならダメージ+38、そうでなければ+10").hpCost(7).requiredPLevel(12)
                .effect((p, d) -> dmg(p, d, d.getBuffState().isGoodConditionActive() ? 38 : 10)).build());

        CardRegistry.register(b("card_fulfillment", "成就", CardType.ONCE_PER_LESSON)
                .description("好調4ターン付与、以後ターン終了毎にダメージ+10（継続）").hpCost(10).requiredPLevel(41)
                .effect((p, d) -> { good(d, 4); d.getBuffState().addParamPerTurn(10); }).build());

        CardRegistry.register(b("card_charming_performance", "魅惑のパフォーマンス", CardType.ONCE_PER_LESSON)
                .description("好調中なら絶好調2ターン付与、以後ターン終了毎にダメージ+6（継続）").hpCost(8).requiredPLevel(43)
                .effect((p, d) -> { if (d.getBuffState().isGoodConditionActive()) great(d, 2); d.getBuffState().addParamPerTurn(6); }).build());

        CardRegistry.register(b("card_supreme_entertainment", "至高のエンタメ", CardType.ONCE_PER_LESSON)
                .description("集中3消費、以後ターン終了毎にダメージ+4（継続）").hpCost(0).requiredPLevel(50)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((p, d) -> { d.getBuffState().addFocus(-3); d.getBuffState().addParamPerTurn(4); }).build());

        CardRegistry.register(b("card_awakening", "覚醒", CardType.ONCE_PER_LESSON)
                .description("好調消費、ダメージ+3を2回、集中+4").hpCost(0).requiredPLevel(53)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((p, d) -> { dmg(p, d, 3); dmg(p, d, 3); d.getBuffState().addFocus(4); }).build());

        CardRegistry.register(b("card_limelight", "脚光", CardType.ONCE_PER_LESSON)
                .description("好調消費、集中+7、追加でもう1枚使用可、以後ターン終了毎にダメージ+4（継続）").hpCost(0).requiredPLevel(75)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((p, d) -> { d.getBuffState().addFocus(7); d.getBuffState().addBonusAction(1); d.getBuffState().addParamPerTurn(4); }).build());

        CardRegistry.register(b("card_hot_topic", "話題沸騰", CardType.ONCE_PER_LESSON)
                .description("絶好調が必要。好調8ターン以上ならダメージ+37、そうでなければ+15").hpCost(0).requiredPLevel(75)
                .usableWhen((p, d) -> d.getBuffState().isGreatConditionActive(), "絶好調が必要")
                .effect((p, d) -> dmg(p, d, d.getBuffState().getGoodConditionTurnsAccumulated() >= 8 ? 37 : 15)).build());

        CardRegistry.register(b("card_national_idol", "国民的アイドル", CardType.ONCE_PER_LESSON)
                .description("好調が必要。次に使うカードの効果を2回発動、追加でもう1枚使用可").hpCost(0).requiredPLevel(25)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((p, d) -> { d.getBuffState().addDoubleNextCards(1); d.getBuffState().addBonusAction(1); }).build());

        CardRegistry.register(b("card_endless_applause", "鳴り止まない拍手", CardType.ONCE_PER_LESSON)
                .description("集中+4、好調2ターン付与、消費体力減少2ターン").hpCost(5).requiredPLevel(38)
                .effect((p, d) -> { d.getBuffState().addFocus(4); good(d, 2); d.getBuffState().addCostReductionTurns(2); }).build());

        CardRegistry.register(b("card_natural_talent", "天賦の才", CardType.ONCE_PER_LESSON)
                .description("好調3ターン付与、集中+2").hpCost(5).requiredPLevel(70)
                .effect((p, d) -> { good(d, 3); d.getBuffState().addFocus(2); }).build());
    }
}
