package com.gakumas.produce.card.impl;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.buff.BuffState;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.card.CardType;
import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.registry.ModItems;
import com.gakumas.produce.util.GenkiHelper;
import com.gakumas.produce.util.ScoreMath;
import com.gakumas.produce.util.TargetingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 全67枚のカード定義。学マスWiki準拠。
 * 「〇ターン」は 1ターン=5秒=100Tick として実時間換算。
 */
public final class ProduceCards {

    private ProduceCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, path);
    }

    public static void registerAll() {

        // ================================================================
        // センス アクティブ 基本カード
        // ================================================================

        // 1. アピールの基本: コスト4, パラメータ+13
        CardRegistry.register(CardDefinition.builder(id("card_appeal_basic"), "アピールの基本", CardType.NORMAL)
                .description("パラメータ+13")
                .hpCost(4).baseScore(13)
                .effect((player, deck) -> dmg(player, deck, 13, 1.5))
                .build());

        // 2. ステージングの基本: コスト2, 好調中のみ, パラメータ+10(好調効果2倍)
        CardRegistry.register(CardDefinition.builder(id("card_staging_basic"), "ステージングの基本", CardType.NORMAL)
                .description("パラメータ+10（好調効果2倍）")
                .hpCost(2).baseScore(10)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> dmgGm(player, deck, 10, ScoreMath.GOOD_MULT_DOUBLE))
                .build());

        // 3. ステップの基本: コスト3, パラメータ+6, 好調2ターン
        CardRegistry.register(CardDefinition.builder(id("card_step_basic"), "ステップの基本", CardType.NORMAL)
                .description("パラメータ+6、好調2ターン")
                .hpCost(3).baseScore(6)
                .effect((player, deck) -> {
                    dmg(player, deck, 6, 1.5);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 4. パフォーマンスの基本: コスト3, パラメータ+6, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_performance_basic"), "パフォーマンスの基本", CardType.NORMAL)
                .description("パラメータ+6、集中+2")
                .hpCost(3).baseScore(6)
                .effect((player, deck) -> {
                    dmg(player, deck, 6, 1.5);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 5. リアクションの基本: コスト2, 集中3以上, パラメータ+5を2回
        CardRegistry.register(CardDefinition.builder(id("card_reaction_basic"), "リアクションの基本", CardType.NORMAL)
                .description("パラメータ+5を2回")
                .hpCost(2).baseScore(10)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) {
                        var s = player.level().damageSources().magic();
                        BuffState b = deck.getBuffState();
                        t.hurt(s, ScoreMath.calculateDamage(5, b));
                        t.hurt(s, ScoreMath.calculateDamage(5, b));
                    }
                }).build());

        // 6. 挑戦: コスト7, 好調中のみ, パラメータ+25
        CardRegistry.register(CardDefinition.builder(id("card_challenge"), "挑戦", CardType.NORMAL)
                .description("パラメータ+25（好調中のみ）")
                .hpCost(7).baseScore(25)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> dmg(player, deck, 25, 1.5))
                .build());

        // 7. 試行錯誤: コスト7, パラメータ+8を2回
        CardRegistry.register(CardDefinition.builder(id("card_trial_error"), "試行錯誤", CardType.NORMAL)
                .description("パラメータ+8を2回")
                .hpCost(7).baseScore(16)
                .effect((player, deck) -> {
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) {
                        var s = player.level().damageSources().magic();
                        BuffState b = deck.getBuffState();
                        t.hurt(s, ScoreMath.calculateDamage(8, b));
                        t.hurt(s, ScoreMath.calculateDamage(8, b));
                    }
                }).build());

        // 8. ハイタッチ: コスト4, パラメータ+17(集中効果1.5倍)
        CardRegistry.register(CardDefinition.builder(id("card_high_touch"), "ハイタッチ", CardType.NORMAL)
                .description("パラメータ+17（集中ボーナス1.5倍）")
                .hpCost(4).baseScore(17)
                .effect((player, deck) -> {
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) {
                        BuffState b = deck.getBuffState();
                        int raw = 17 + (int)(b.getFocusStacks() * 1.5);
                        double mult = b.isGoodConditionActive()
                                ? (b.isGreatConditionActive() ? 1.5 + 0.1 * b.getGoodConditionTurnsAccumulated() : 1.5)
                                : 1.0;
                        t.hurt(player.level().damageSources().magic(), (int)Math.ceil(raw * mult));
                    }
                }).build());

        // 9. トークタイム: コスト6, 好調中のみ, パラメータ+27
        CardRegistry.register(CardDefinition.builder(id("card_talk_time"), "トークタイム", CardType.NORMAL)
                .description("パラメータ+27（好調中のみ）")
                .hpCost(6).baseScore(27)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> dmg(player, deck, 27, 1.5))
                .build());

        // 10. 脚光: コスト4, パラメータ+6, 集中+2, 手札のアクティブ2枚以上でパラメータ+3
        CardRegistry.register(CardDefinition.builder(id("card_limelight"), "脚光", CardType.NORMAL)
                .description("パラメータ+6(条件で+3)、集中+2")
                .hpCost(4).baseScore(6)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    // 手札のアクティブカード数簡易判定: 手札2枚以上でボーナス
                    int bonus = deck.getHand().size() >= 2 ? 3 : 0;
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) t.hurt(player.level().damageSources().magic(),
                            ScoreMath.calculateDamage(6 + bonus, b));
                    b.addFocus(2);
                }).build());

        // 11. 話題沸騰: コスト5, パラメータ+13, 集中6以上で+15追加
        CardRegistry.register(CardDefinition.builder(id("card_hot_topic"), "話題沸騰", CardType.NORMAL)
                .description("パラメータ+13、集中6以上でさらに+15")
                .hpCost(5).baseScore(13)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    int base = b.getFocusStacks() >= 6 ? 28 : 13;
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) t.hurt(player.level().damageSources().magic(),
                            ScoreMath.calculateDamage(base, b));
                }).build());

        // 12. スリリング: コスト5, パラメータ+11
        CardRegistry.register(CardDefinition.builder(id("card_thrilling"), "スリリング", CardType.NORMAL)
                .description("パラメータ+11")
                .hpCost(5).baseScore(11)
                .effect((player, deck) -> dmg(player, deck, 11, 1.5))
                .build());

        // 13. 大胆不敵: コスト5, パラメータ+8
        CardRegistry.register(CardDefinition.builder(id("card_fearless"), "大胆不敵", CardType.NORMAL)
                .description("パラメータ+8")
                .hpCost(5).baseScore(8)
                .effect((player, deck) -> dmg(player, deck, 8, 1.5))
                .build());

        // 14. 魅惑のパフォーマンス: コスト5, 絶好調中のみ, パラメータ+15
        CardRegistry.register(CardDefinition.builder(id("card_charming_performance"), "魅惑のパフォーマンス", CardType.NORMAL)
                .description("パラメータ+15（絶好調中のみ）")
                .hpCost(5).baseScore(15)
                .usableWhen((p, d) -> d.getBuffState().isGreatConditionActive(), "絶好調が必要")
                .effect((player, deck) -> dmg(player, deck, 15, 1.5))
                .build());

        // 15. アドリブ: コスト4, パラメータ+6, 好調2ターン
        CardRegistry.register(CardDefinition.builder(id("card_adlib"), "アドリブ", CardType.NORMAL)
                .description("パラメータ+6、好調2ターン")
                .hpCost(4).baseScore(6)
                .effect((player, deck) -> {
                    dmg(player, deck, 6, 1.5);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 16. 盛り上げ: コスト4, パラメータ+6, 集中+2, 手札2枚以上でパラメータ+4
        CardRegistry.register(CardDefinition.builder(id("card_pump_up"), "盛り上げ", CardType.NORMAL)
                .description("パラメータ+6(条件で+4)、集中+2")
                .hpCost(4).baseScore(6)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    int base = deck.getHand().size() >= 2 ? 10 : 6;
                    LivingEntity t = TargetingHelper.getLookTarget(player);
                    if (t != null) t.hurt(player.level().damageSources().magic(),
                            ScoreMath.calculateDamage(base, b));
                    b.addFocus(2);
                }).build());

        // 17. 進路修正: コスト3, 集中2, 好調3ターン, 絶好調中なら集中+4
        CardRegistry.register(CardDefinition.builder(id("card_course_correction"), "進路修正", CardType.NORMAL)
                .description("集中+2、好調3ターン（絶好調中は集中+4）")
                .hpCost(3)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(b.isGreatConditionActive() ? 4 : 2);
                    b.addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                }).build());

        // 18. 願いの力: コスト4, 集中+3, 好調2ターン, 絶好調中なら集中+5
        CardRegistry.register(CardDefinition.builder(id("card_power_of_wish"), "願いの力", CardType.NORMAL)
                .description("集中+3、好調2ターン（絶好調中は集中+5）")
                .hpCost(4)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(b.isGreatConditionActive() ? 5 : 3);
                    b.addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 19. 鋭い目: コスト3, 集中+3, 好調3ターン
        CardRegistry.register(CardDefinition.builder(id("card_keen_eye"), "鋭い目", CardType.NORMAL)
                .description("集中+3、好調3ターン")
                .hpCost(3)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(3);
                    b.addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                }).build());

        // ================================================================
        // センス サポーター / バフカード
        // ================================================================

        // 20. 視線の基本: コスト2, 元気+5, 好調2ターン
        CardRegistry.register(CardDefinition.builder(id("card_gaze_basic"), "視線の基本", CardType.NORMAL)
                .description("元気+5、好調2ターン")
                .hpCost(2)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 5f);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 21. 思考の基本: コスト4, 好調2ターン, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_thinking_basic"), "思考の基本", CardType.NORMAL)
                .description("好調2ターン、集中+2")
                .hpCost(4)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 22. 沈着の基本: コスト2, 元気+5, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_composure_basic"), "沈着の基本", CardType.NORMAL)
                .description("元気+5、集中+2")
                .hpCost(2)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 5f);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 23. タイミングの基本: コスト0, 元気+5, 絶好調1ターン
        CardRegistry.register(CardDefinition.builder(id("card_timing_basic"), "タイミングの基本", CardType.NORMAL)
                .description("元気+5、絶好調1ターン")
                .hpCost(0)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 5f);
                    deck.getBuffState().addGreatCondition(DeckService.TICKS_PER_TURN);
                }).build());

        // 24. 振る舞いの基本: コスト1, 元気+1, 好調2ターン
        CardRegistry.register(CardDefinition.builder(id("card_behavior_basic"), "振る舞いの基本", CardType.NORMAL)
                .description("元気+1、好調2ターン")
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 25. 表情の基本: コスト1, 元気+1, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_facial_basic"), "表情の基本", CardType.NORMAL)
                .description("元気+1、集中+2")
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 26. 軽やかな足取り: コスト5, 元気+5, 集中+3
        CardRegistry.register(CardDefinition.builder(id("card_light_steps"), "軽やかな足取り", CardType.NORMAL)
                .description("元気+5、集中+3")
                .hpCost(5)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 5f);
                    deck.getBuffState().addFocus(3);
                }).build());

        // 27. 魅力: コスト5, 元気+4, 好調2ターン, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_charm"), "魅力", CardType.NORMAL)
                .description("元気+4、好調2ターン、集中+2")
                .hpCost(5)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 4f);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 28. 深呼吸: コスト8, 元気+5, 絶好調3ターン, 集中+3
        CardRegistry.register(CardDefinition.builder(id("card_deep_breath"), "深呼吸", CardType.NORMAL)
                .description("元気+5、絶好調3ターン、集中+3")
                .hpCost(8)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 5f);
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                    deck.getBuffState().addFocus(3);
                }).build());

        // 29. バランス感覚: コスト3, 好調3ターン, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_balance_sense"), "バランス感覚", CardType.NORMAL)
                .description("好調3ターン、集中+2")
                .hpCost(3)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 30. 楽観的: コスト4, 好調2ターン, 集中+3
        CardRegistry.register(CardDefinition.builder(id("card_optimistic"), "楽観的", CardType.NORMAL)
                .description("好調2ターン、集中+3")
                .hpCost(4)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    deck.getBuffState().addFocus(3);
                }).build());

        // 31. ペーシング: コスト3, 好調2ターン, 集中+1
        CardRegistry.register(CardDefinition.builder(id("card_pacing"), "ペーシング", CardType.NORMAL)
                .description("好調2ターン、集中+1")
                .hpCost(3)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    deck.getBuffState().addFocus(1);
                }).build());

        // 32. ウォームアップ: コスト4, 絶好調1ターン, 集中+2
        CardRegistry.register(CardDefinition.builder(id("card_warmup"), "ウォームアップ", CardType.NORMAL)
                .description("絶好調1ターン、集中+2")
                .hpCost(4)
                .effect((player, deck) -> {
                    deck.getBuffState().addGreatCondition(DeckService.TICKS_PER_TURN);
                    deck.getBuffState().addFocus(2);
                }).build());

        // 33. ファンサービス: コスト5, パラメータ+5, 好調3ターン
        CardRegistry.register(CardDefinition.builder(id("card_fan_service"), "ファンサービス", CardType.NORMAL)
                .description("パラメータ+5、好調3ターン")
                .hpCost(5).baseScore(5)
                .effect((player, deck) -> {
                    dmg(player, deck, 5, 1.5);
                    deck.getBuffState().addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                }).build());

        // 34. 勢い: コスト4, 元気+3, 集中+4
        CardRegistry.register(CardDefinition.builder(id("card_momentum"), "勢い", CardType.NORMAL)
                .description("元気+3、集中+4")
                .hpCost(4)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 3f);
                    deck.getBuffState().addFocus(4);
                }).build());

        // ================================================================
        // センス レッスン中1回 / 重複不可
        // ================================================================

        // 35. 表現の基本: レッスン中1回, コスト0, 元気+4
        CardRegistry.register(CardDefinition.builder(id("card_expression_basic"), "表現の基本", CardType.ONCE_PER_LESSON)
                .description("元気+4")
                .hpCost(0)
                .effect((player, deck) -> GenkiHelper.addGenki(player, 4f))
                .build());

        // 36. 静かな意志: PLv20, レッスン中1回, コスト4, 集中+3, 好調2ターン, 初手確定
        CardRegistry.register(CardDefinition.builder(id("card_quiet_will"), "静かな意志", CardType.ONCE_PER_LESSON)
                .description("集中+3、好調2ターン（初手確定）")
                .hpCost(4).requiredPLevel(20).guaranteedFirstDraw(true)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(3);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 37. 演出計画: PLv17, レッスン中1回, コスト4, 絶好調3ターン, 以後カード使用毎に元気+2
        CardRegistry.register(CardDefinition.builder(id("card_direction_plan"), "演出計画", CardType.ONCE_PER_LESSON)
                .description("絶好調3ターン、以後カード使用毎に元気+2（重複可）")
                .hpCost(4).requiredPLevel(17)
                .effect((player, deck) -> {
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                    BuffState b = deck.getBuffState();
                    b.setCustomCounter("encore_genki_stacks", b.getCustomCounter("encore_genki_stacks") + 1);
                }).build());

        // 38. コール&レスポンス: PLv11, レッスン中1回, コスト6, パラメータ+15, 集中3以上で+30
        CardRegistry.register(CardDefinition.builder(id("card_call_response"), "コール&レスポンス", CardType.ONCE_PER_LESSON)
                .description("パラメータ+15、集中3以上で+30")
                .hpCost(6).requiredPLevel(11).baseScore(15)
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    int base = b.getFocusStacks() >= 3 ? 30 : 15;
                    dmg(player, deck, base, 1.5);
                }).build());

        // 39. エキサイト: PLv56, レッスン中1回, コスト4, パラメータ+6, 絶好調3ターン
        CardRegistry.register(CardDefinition.builder(id("card_excite"), "エキサイト", CardType.ONCE_PER_LESSON)
                .description("パラメータ+6、絶好調3ターン")
                .hpCost(4).baseScore(6).requiredPLevel(56)
                .effect((player, deck) -> {
                    dmg(player, deck, 6, 1.5);
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                }).build());

        // 40. 決めポーズ: PLv20, レッスン中1回, コスト4, 好調2ターン, 集中+3
        CardRegistry.register(CardDefinition.builder(id("card_decided_pose"), "決めポーズ", CardType.ONCE_PER_LESSON)
                .description("好調2ターン、集中+3")
                .hpCost(4).requiredPLevel(20)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    deck.getBuffState().addFocus(3);
                }).build());

        // 41. 跳躍: PLv28, レッスン中1回, コスト3, 集中+3, 好調2ターン
        CardRegistry.register(CardDefinition.builder(id("card_leap"), "跳躍", CardType.ONCE_PER_LESSON)
                .description("集中+3、好調2ターン")
                .hpCost(3).requiredPLevel(28)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(3);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                }).build());

        // 42. 祝福: PLv24, レッスン中1回, コスト5, 絶好調3ターン, 集中+4
        CardRegistry.register(CardDefinition.builder(id("card_blessing"), "祝福", CardType.ONCE_PER_LESSON)
                .description("絶好調3ターン、集中+4")
                .hpCost(5).requiredPLevel(24)
                .effect((player, deck) -> {
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                    deck.getBuffState().addFocus(4);
                }).build());

        // 43. スタートダッシュ: PLv22, レッスン中1回, コスト5, パラメータ+30, 元気+10, 体力消費2ターン
        CardRegistry.register(CardDefinition.builder(id("card_start_dash"), "スタートダッシュ", CardType.ONCE_PER_LESSON)
                .description("パラメータ+30、元気+10、体力消費+2(2ターン)")
                .hpCost(5).baseScore(30).requiredPLevel(22)
                .effect((player, deck) -> {
                    dmg(player, deck, 30, 1.5);
                    GenkiHelper.addGenki(player, 10f);
                    deck.getBuffState().addCostReductionTurns(-2); // 体力消費増加
                }).build());

        // 44. スタンドプレー: PLv24, レッスン中1回, コスト5, パラメータ+12, 元気+7, 集中+5, 体力消費2ターン
        CardRegistry.register(CardDefinition.builder(id("card_stand_play"), "スタンドプレー", CardType.ONCE_PER_LESSON)
                .description("パラメータ+12、元気+7、集中+5、体力消費+2(2ターン)")
                .hpCost(5).baseScore(12).requiredPLevel(24)
                .effect((player, deck) -> {
                    dmg(player, deck, 12, 1.5);
                    GenkiHelper.addGenki(player, 7f);
                    deck.getBuffState().addFocus(5);
                    deck.getBuffState().addCostReductionTurns(-2);
                }).build());

        // 45. ポーズの基本: コスト3, パラメータ+2, 元気+2
        CardRegistry.register(CardDefinition.builder(id("card_pose_basic"), "ポーズの基本", CardType.NORMAL)
                .description("パラメータ+2、元気+2")
                .hpCost(3).baseScore(2)
                .effect((player, deck) -> {
                    dmg(player, deck, 2, 1.5);
                    GenkiHelper.addGenki(player, 2f);
                }).build());

        // ================================================================
        // シュプレヒコール (PLv33): 集中消費3, パラメータ+6, 好調2ターン, 追加使用可
        // ================================================================
        CardRegistry.register(CardDefinition.builder(id("card_shofu_hiko"), "シュプレヒコール", CardType.NORMAL)
                .description("集中3消費、パラメータ+6、好調2ターン、追加使用可")
                .hpCost(0).baseScore(6).requiredPLevel(33)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(-3);
                    dmg(player, deck, 6, 1.5);
                    b.addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    b.addBonusAction(1);
                }).build());

        // ================================================================
        // 存在感 (PLv31): 好調2ターン消費, 集中+4, 追加使用可
        // ================================================================
        CardRegistry.register(CardDefinition.builder(id("card_existence"), "存在感", CardType.NORMAL)
                .description("好調2ターン消費、集中+4、追加使用可")
                .hpCost(0).requiredPLevel(31)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    int consume = Math.min(b.getGoodConditionTicks(), 2 * DeckService.TICKS_PER_TURN);
                    b.addGoodCondition(-consume, 0);
                    b.addFocus(4);
                    b.addBonusAction(1);
                }).build());

        // ================================================================
        // 魅惑の視線 (PLv30): 集中消費3, 絶好調4ターン, 追加使用可
        // ================================================================
        CardRegistry.register(CardDefinition.builder(id("card_fascination"), "魅惑の視線", CardType.ONCE_PER_LESSON)
                .description("集中3消費、絶好調4ターン、追加使用可")
                .hpCost(0).requiredPLevel(30)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(-3);
                    b.addGreatCondition(4 * DeckService.TICKS_PER_TURN);
                    b.addBonusAction(1);
                }).build());

        // ================================================================
        // 天真爛漫 (PLv45): コスト6, 集中+1, ターン終了時集中3以上で集中+2
        // ================================================================
        CardRegistry.register(CardDefinition.builder(id("card_innocence"), "天真爛漫", CardType.ONCE_PER_LESSON)
                .description("集中+1、ターン終了時集中3以上で集中+2（重複可）")
                .hpCost(6).requiredPLevel(45)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(1);
                    BuffState b = deck.getBuffState();
                    b.setCustomCounter("focus_per_turn_stacks", b.getCustomCounter("focus_per_turn_stacks") + 1);
                }).build());

        // ================================================================
        // 追加カード: レッスン中1回 重複不可系
        // ================================================================

        // 一息: PLv47, レッスン中1回, コスト7, 好調3ターン, 集中+4
        CardRegistry.register(CardDefinition.builder(id("card_one_breath"), "一息", CardType.ONCE_PER_LESSON)
                .description("好調3ターン、集中+4")
                .hpCost(7).requiredPLevel(47)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                    deck.getBuffState().addFocus(4);
                }).build());

        // 情熱ターン: PLv38, レッスン中1回, コスト3, 集中+1, ターン終了時集中+2
        CardRegistry.register(CardDefinition.builder(id("card_passion_turn"), "情熱ターン", CardType.ONCE_PER_LESSON)
                .description("集中+1、ターン終了時集中3以上で集中+2（重複可）")
                .hpCost(3).requiredPLevel(38)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(1);
                    BuffState b = deck.getBuffState();
                    b.setCustomCounter("focus_per_turn_stacks", b.getCustomCounter("focus_per_turn_stacks") + 1);
                }).build());

        // 立ち位置チェック: PLv51, レッスン中1回, 集中消費3, パラメータ+30(好調2倍), 元気+15
        CardRegistry.register(CardDefinition.builder(id("card_position_check"), "立ち位置チェック", CardType.ONCE_PER_LESSON)
                .description("集中3消費、パラメータ+30(好調2倍)、元気+15")
                .hpCost(0).baseScore(30).requiredPLevel(51)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(-3);
                    dmgGm(player, deck, 30, ScoreMath.GOOD_MULT_DOUBLE);
                    GenkiHelper.addGenki(player, 15f);
                }).build());

        // 止められない: PLv45, レッスン中1回, コスト7, パラメータ+18
        CardRegistry.register(CardDefinition.builder(id("card_unstoppable"), "止められない", CardType.ONCE_PER_LESSON)
                .description("パラメータ+18")
                .hpCost(7).baseScore(18).requiredPLevel(45)
                .effect((player, deck) -> dmg(player, deck, 18, 1.5))
                .build());

        // 大声援: PLv49, レッスン中1回, 体力3, 元気+7, 好調5ターン
        CardRegistry.register(CardDefinition.builder(id("card_big_cheer"), "大声援", CardType.ONCE_PER_LESSON)
                .description("元気+7、好調5ターン")
                .hpCost(3).requiredPLevel(49)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 7f);
                    deck.getBuffState().addGoodCondition(5 * DeckService.TICKS_PER_TURN, 5);
                }).build());

        // スタートの合図: PLv53, レッスン中1回, コスト5, 集中+7, 追加使用可
        CardRegistry.register(CardDefinition.builder(id("card_starting_signal"), "スタートの合図", CardType.ONCE_PER_LESSON)
                .description("集中+7、追加使用可")
                .hpCost(5).requiredPLevel(53)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(7);
                    deck.getBuffState().addBonusAction(1);
                }).build());

        // 根性: PLv29, レッスン中1回, コスト5, 元気+3, 集中+4
        CardRegistry.register(CardDefinition.builder(id("card_grit"), "根性", CardType.ONCE_PER_LESSON)
                .description("元気+3、集中+4")
                .hpCost(5).requiredPLevel(29)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 3f);
                    deck.getBuffState().addFocus(4);
                }).build());

        // 成功への道: PLv36, レッスン中1回, コスト5, 好調3ターン, 集中+4
        CardRegistry.register(CardDefinition.builder(id("card_path_to_success"), "成功への道", CardType.ONCE_PER_LESSON)
                .description("好調3ターン、集中+4")
                .hpCost(5).requiredPLevel(36)
                .effect((player, deck) -> {
                    deck.getBuffState().addGoodCondition(3 * DeckService.TICKS_PER_TURN, 3);
                    deck.getBuffState().addFocus(4);
                }).build());

        // スポットライト: PLv49, レッスン中1回, 体力3, 元気+7, 好調5ターン
        CardRegistry.register(CardDefinition.builder(id("card_spotlight"), "スポットライト", CardType.ONCE_PER_LESSON)
                .description("元気+7、好調5ターン、追加ドロー+1")
                .hpCost(3).requiredPLevel(49)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 7f);
                    deck.getBuffState().addGoodCondition(5 * DeckService.TICKS_PER_TURN, 5);
                    deck.getBuffState().addPendingDraw(1);
                }).build());

        // 一発勝負: PLv53, レッスン中1回, コスト5, 集中+7, 追加使用可, 手札ドロー
        CardRegistry.register(CardDefinition.builder(id("card_one_shot"), "一発勝負", CardType.ONCE_PER_LESSON)
                .description("集中+7、追加使用可、手札からドロー")
                .hpCost(5).requiredPLevel(53)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(7);
                    deck.getBuffState().addBonusAction(1);
                    deck.getBuffState().addPendingDraw(1);
                }).build());

        // 心の結束: PLv54, レッスン中1回, コスト4, 元気+3, 集中+5
        CardRegistry.register(CardDefinition.builder(id("card_mental_unity"), "心の結束", CardType.ONCE_PER_LESSON)
                .description("元気+3、集中+5")
                .hpCost(4).requiredPLevel(54)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 3f);
                    deck.getBuffState().addFocus(5);
                }).build());

        // バズワード: PLv52, レッスン中1回, コスト5, 集中+4, 追加使用可
        CardRegistry.register(CardDefinition.builder(id("card_buzzword"), "バズワード", CardType.ONCE_PER_LESSON)
                .description("集中+4、追加使用可")
                .hpCost(5).requiredPLevel(52)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(4);
                    deck.getBuffState().addBonusAction(1);
                }).build());

        // 充足感: PLv70, レッスン中1回, 重複不可, コスト4, 元気+2, パラメータ+5
        CardRegistry.register(CardDefinition.builder(id("card_fulfillment"), "充足感", CardType.ONCE_PER_LESSON)
                .description("元気+2、パラメータ+5")
                .hpCost(4).baseScore(5).requiredPLevel(70)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 2f);
                    dmg(player, deck, 5, 1.5);
                }).build());

        // 至高のエンタメ: PLv74, レッスン中1回, 重複不可, コスト3, 元気+6, 集中+5, 継続パラメータ+5(3ターン)
        CardRegistry.register(CardDefinition.builder(id("card_supreme_entertainment"), "至高のエンタメ", CardType.ONCE_PER_LESSON)
                .description("元気+6、集中+5、毎ターン開始時パラメータ+5(3ターン)")
                .hpCost(3).requiredPLevel(74)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 6f);
                    deck.getBuffState().addFocus(5);
                    deck.getBuffState().addParamPerTurn(5);
                }).build());

        // 覚醒: PLv75, レッスン中1回, 重複不可, コスト4, パラメータ+20, 集中+5
        CardRegistry.register(CardDefinition.builder(id("card_awakening"), "覚醒", CardType.ONCE_PER_LESSON)
                .description("パラメータ+20、集中+5")
                .hpCost(4).baseScore(20).requiredPLevel(75)
                .effect((player, deck) -> {
                    dmg(player, deck, 20, 1.5);
                    deck.getBuffState().addFocus(5);
                }).build());

        // 国民的アイドル: PLv25, レッスン中1回, 重複不可, 好調消費1, 次カード2回発動, 追加使用可
        CardRegistry.register(CardDefinition.builder(id("card_national_idol"), "国民的アイドル", CardType.ONCE_PER_LESSON)
                .description("好調1消費、次カード2回発動、追加使用可")
                .hpCost(0).requiredPLevel(25)
                .usableWhen((p, d) -> d.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addGoodCondition(-DeckService.TICKS_PER_TURN, 0);
                    b.addDoubleNextCards(1);
                    b.addBonusAction(1);
                }).build());

        // 終わらない拍手: PLv75, レッスン中1回, 重複不可, 絶好調消費1, パラメータ+37(好調2倍)
        CardRegistry.register(CardDefinition.builder(id("card_endless_applause"), "終わらない拍手", CardType.ONCE_PER_LESSON)
                .description("絶好調1消費、パラメータ+37(好調2倍)")
                .hpCost(0).baseScore(37).requiredPLevel(75)
                .usableWhen((p, d) -> d.getBuffState().isGreatConditionActive(), "絶好調が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addGreatCondition(-DeckService.TICKS_PER_TURN);
                    dmgGm(player, deck, 37, ScoreMath.GOOD_MULT_DOUBLE);
                }).build());

        // 天賦の才: PLv50, レッスン中1回, 重複不可, 集中消費3, 以後アクティブカード使用毎にパラメータ+4
        CardRegistry.register(CardDefinition.builder(id("card_natural_talent"), "天賦の才", CardType.ONCE_PER_LESSON)
                .description("集中3消費、アクティブカード使用毎にパラメータ+4（重複可）")
                .hpCost(0).requiredPLevel(50)
                .usableWhen((p, d) -> d.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState b = deck.getBuffState();
                    b.addFocus(-3);
                    b.setCustomCounter("encore_genki_stacks", b.getCustomCounter("encore_genki_stacks") + 2);
                }).build());
    }

    // ================================================================
    // ヘルパーメソッド
    // ================================================================

    private static void dmg(net.minecraft.server.level.ServerPlayer player,
                            com.gakumas.produce.capability.IDeckData deck,
                            int base, double goodMult) {
        LivingEntity t = TargetingHelper.getLookTarget(player);
        if (t != null) {
            t.hurt(player.level().damageSources().magic(),
                    ScoreMath.calculateDamage(base, deck.getBuffState()));
        }
    }

    private static void dmgGm(net.minecraft.server.level.ServerPlayer player,
                               com.gakumas.produce.capability.IDeckData deck,
                               int base, double goodMult) {
        LivingEntity t = TargetingHelper.getLookTarget(player);
        if (t != null) {
            t.hurt(player.level().damageSources().magic(),
                    ScoreMath.calculateDamage(base, deck.getBuffState(), goodMult));
        }
    }
}
