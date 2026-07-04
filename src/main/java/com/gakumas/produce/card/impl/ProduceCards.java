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
 * 初期実装6カードの定義。
 * 「〇ターン」は仕様書の指示通り、すべて 1ターン=5秒=100Tick として実時間換算している。
 */
public final class ProduceCards {

    private ProduceCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, path);
    }

    /** 上位進捗（テスト用）。データパック側で data/gakumas_produce/advancements/rank/upper.json を用意する想定 */
    public static final ResourceLocation UPPER_RANK_ADVANCEMENT = new ResourceLocation(GakumasProduceMod.MOD_ID, "rank/upper");

    public static void registerAll() {

        // 1. アピールの基本 (アクティブ): コスト体力4。パラメータ+13。
        CardRegistry.register(CardDefinition.builder(id("card_appeal_basic"), "アピールの基本", CardType.NORMAL)
                .description("パラメータ+13")
                .hpCost(4)
                .baseScore(13)
                .effect((player, deck) -> {
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        BuffState buff = deck.getBuffState();
                        int damage = ScoreMath.calculateDamage(13, buff);
                        target.hurt(player.level().damageSources().magic(), damage);
                    }
                })
                .build());

        // 2. 表現の基本 (レッスン中1回): コストなし。元気+4獲得。
        CardRegistry.register(CardDefinition.builder(id("card_expression_basic"), "表現の基本", CardType.ONCE_PER_LESSON)
                .description("元気+4獲得")
                .hpCost(0)
                .effect((player, deck) -> GenkiHelper.addGenki(player, 4f))
                .build());

        // 3. 振る舞いの基本 (通常): コスト体力1。元気+1、自分に好調を2ターン(10秒)付与。
        CardRegistry.register(CardDefinition.builder(id("card_behavior_basic"), "振る舞いの基本", CardType.NORMAL)
                .description("元気+1、好調2ターン付与")
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                })
                .build());

        // 4. 表情の基本 (通常): コスト体力1。元気+1、自分に集中+2付与。
        CardRegistry.register(CardDefinition.builder(id("card_facial_basic"), "表情の基本", CardType.NORMAL)
                .description("元気+1、集中+2付与")
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addFocus(2);
                })
                .build());

        // 5. 静かな意志 (レッスン中1回): 【要:Pレベル20】コスト体力4。集中+3、好調2ターン(10秒)。初手確定。
        //    ※本家wiki(スキルカード一覧)の解放Pレベル PLv20 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_quiet_will"), "静かな意志", CardType.ONCE_PER_LESSON)
                .description("集中+3、好調2ターン付与（初手確定）")
                .hpCost(4)
                .requiredPLevel(20)
                .guaranteedFirstDraw(true)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(3);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                })
                .build());

        // 6. 演出計画 (レッスン中1回): 【要:Pレベル17】コスト体力4。絶好調3ターン(15秒)付与。
        //    さらにデッキリセットまでの間、カード使用毎に元気+2されるパッシブを付与。
        //    ※スタック式なので、デッキに複数枚入れて重複使用すればその分だけ効果が重複発動する。
        //    ※本家wiki(スキルカード一覧)の解放Pレベル PLv17 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_direction_plan"), "演出計画", CardType.ONCE_PER_LESSON)
                .description("絶好調3ターン付与、以後カード使用毎に元気+2（重複使用でさらに加算）")
                .hpCost(4)
                .requiredPLevel(17)
                .effect((player, deck) -> {
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                    BuffState buff = deck.getBuffState();
                    buff.setCustomCounter("encore_genki_stacks", buff.getCustomCounter("encore_genki_stacks") + 1);
                })
                .build());

        // 7. シュプレヒコール (通常 / 金): 【要:Pレベル33】集中消費3、パラメータ+6、好調2ターン付与、追加でもう1枚使用可。
        //    ※本家の「消費体力減少」は本Modでは未実装のため簡略化している。本家wikiの解放Pレベル PLv33 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_shofu_hiko"), "シュプレヒコール", CardType.NORMAL)
                .description("集中3消費、ダメージ+6、好調2ターン付与、追加でもう1枚使用可")
                .hpCost(0)
                .baseScore(6)
                .requiredPLevel(33)
                .usableWhen((player, deck) -> deck.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    buff.addFocus(-3);
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(6, buff));
                    }
                    buff.addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                    buff.addBonusAction(1);
                })
                .build());

        // 8. 存在感 (通常 / 金): 【要:Pレベル31】好調を2ターン分消費し、集中+4、追加でもう1枚使用可。
        //    ※本家wiki(スキルカード一覧)の解放Pレベル PLv31 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_existence"), "存在感", CardType.NORMAL)
                .description("好調2ターン消費、集中+4、追加でもう1枚使用可")
                .hpCost(0)
                .requiredPLevel(31)
                .usableWhen((player, deck) -> deck.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    int consumeTicks = Math.min(buff.getGoodConditionTicks(), 2 * DeckService.TICKS_PER_TURN);
                    buff.addGoodCondition(-consumeTicks, 0);
                    buff.addFocus(4);
                    buff.addBonusAction(1);
                })
                .build());

        // 9. 魅惑の視線 (レッスン中1回 / 虹): 【要:Pレベル30】集中消費3、絶好調4ターン付与、追加でもう1枚使用可。
        //    ※本家の「消費体力減少」は本Modでは未実装のため簡略化している。本家wikiの解放Pレベル PLv30 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_fascination"), "魅惑の視線", CardType.ONCE_PER_LESSON)
                .description("集中3消費、絶好調4ターン付与、追加でもう1枚使用可")
                .hpCost(0)
                .requiredPLevel(30)
                .usableWhen((player, deck) -> deck.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    buff.addFocus(-3);
                    buff.addGreatCondition(4 * DeckService.TICKS_PER_TURN);
                    buff.addBonusAction(1);
                })
                .build());

        // 10. 天真爛漫 (レッスン中1回 / 虹): 【要:Pレベル45】コスト体力6。集中+1。以後ターン終了時、集中3以上なら集中+2。
        // ※スタック式なので、デッキに複数枚入れて重複使用すればその分だけ効果が重複発動する。
        // ※本家wiki(スキルカード一覧)の解放Pレベル PLv45 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_innocence"), "天真爛漫", CardType.ONCE_PER_LESSON)
                .description("集中+1、以後ターン終了時に集中3以上なら集中+2（重複使用でさらに加算）")
                .hpCost(6)
                .requiredPLevel(45)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(1);
                    BuffState buff = deck.getBuffState();
                    buff.setCustomCounter("focus_per_turn_stacks", buff.getCustomCounter("focus_per_turn_stacks") + 1);
                })
                .build());

        // 11. コール&レスポンス (レッスン中1回 / 虹): 【要:Pレベル11】コスト体力6。パラメータ+15、集中3以上ならさらに+15。
        //    ※本家Wikiで「PLv11解放」と確認できたためそのまま反映。
        CardRegistry.register(CardDefinition.builder(id("card_call_response"), "コール&レスポンス", CardType.ONCE_PER_LESSON)
                .description("ダメージ+15、集中3以上でさらに+15")
                .hpCost(6)
                .requiredPLevel(11)
                .baseScore(15)
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    int base = buff.getFocusStacks() >= 3 ? 30 : 15;
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(base, buff));
                    }
                })
                .build());

        // 12. エキサイト (レッスン中1回 / 銀): 【要:Pレベル56】コスト体力4。パラメータ+6、絶好調3ターン付与。
        //    ※本家wiki(スキルカード一覧)の解放Pレベル PLv56 に一致。
        CardRegistry.register(CardDefinition.builder(id("card_excite"), "エキサイト", CardType.ONCE_PER_LESSON)
                .description("ダメージ+6、絶好調3ターン付与")
                .hpCost(4)
                .baseScore(6)
                .requiredPLevel(56)
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(6, buff));
                    }
                    buff.addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                })
                .build());

        // 13. ステージングの基本 (アクティブ / センス): コスト2。好調2ターン以上で使用可。パラメータ+10（好調効果2倍）。
        CardRegistry.register(CardDefinition.builder(id("card_staging_basic"), "ステージングの基本", CardType.NORMAL)
                .description("パラメータ+10（好調効果2倍）")
                .hpCost(2)
                .baseScore(10)
                .usableWhen((player, deck) -> deck.getBuffState().isGoodConditionActive(), "好調が必要")
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(),
                                ScoreMath.calculateDamage(10, buff, ScoreMath.GOOD_MULT_DOUBLE));
                    }
                })
                .build());

        // 14. ステップの基本 (通常 / センス): コスト体力3。パラメータ+6、好調2ターン付与。
        CardRegistry.register(CardDefinition.builder(id("card_step_basic"), "ステップの基本", CardType.NORMAL)
                .description("ダメージ+6、好調2ターン付与")
                .hpCost(3)
                .baseScore(6)
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(6, buff));
                    }
                    buff.addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                })
                .build());

        // 15. パフォーマンスの基本 (通常 / センス): コスト体力3。パラメータ+6、集中+2。
        CardRegistry.register(CardDefinition.builder(id("card_performance_basic"), "パフォーマンスの基本", CardType.NORMAL)
                .description("ダメージ+6、集中+2")
                .hpCost(3)
                .baseScore(6)
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(6, buff));
                    }
                    buff.addFocus(2);
                })
                .build());

        // 16. リアクションの基本 (通常 / センス): コスト体力2。パラメータ+5を2回（集中3以上で使用可）。
        CardRegistry.register(CardDefinition.builder(id("card_reaction_basic"), "リアクションの基本", CardType.NORMAL)
                .description("ダメージ+5を2回")
                .hpCost(2)
                .baseScore(10)
                .usableWhen((player, deck) -> deck.getBuffState().getFocusStacks() >= 3, "集中3以上が必要")
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        var src = player.level().damageSources().magic();
                        target.hurt(src, ScoreMath.calculateDamage(5, buff));
                        target.hurt(src, ScoreMath.calculateDamage(5, buff));
                    }
                })
                .build());

        // 17. ポーズの基本 (通常 / フリー): コスト体力3。パラメータ+2、元気+2。
        CardRegistry.register(CardDefinition.builder(id("card_pose_basic"), "ポーズの基本", CardType.NORMAL)
                .description("ダメージ+2、元気+2")
                .hpCost(3)
                .baseScore(2)
                .effect((player, deck) -> {
                    BuffState buff = deck.getBuffState();
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        target.hurt(player.level().damageSources().magic(), ScoreMath.calculateDamage(2, buff));
                    }
                    GenkiHelper.addGenki(player, 2f);
                })
                .build());
    }
}
