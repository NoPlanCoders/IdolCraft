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

        // 1. アピールの基本 (Lカード): コスト体力4。ターゲットに9の魔法ダメージ。
        CardRegistry.register(CardDefinition.builder(id("card_appeal_basic"), "アピールの基本", CardType.L_CARD)
                .hpCost(4)
                .baseScore(9)
                .effect((player, deck) -> {
                    LivingEntity target = TargetingHelper.getLookTarget(player);
                    if (target != null) {
                        BuffState buff = deck.getBuffState();
                        int damage = ScoreMath.calculateDamage(9, buff);
                        target.hurt(player.level().damageSources().magic(), damage);
                    }
                })
                .build());

        // 2. 表現の基本 (Oカード): コストなし。元気+4獲得。
        CardRegistry.register(CardDefinition.builder(id("card_expression_basic"), "表現の基本", CardType.O_CARD)
                .hpCost(0)
                .effect((player, deck) -> GenkiHelper.addGenki(player, 4f))
                .build());

        // 3. 振る舞いの基本 (Lカード): コスト体力1。元気+1、自分に好調を2ターン(10秒)付与。
        CardRegistry.register(CardDefinition.builder(id("card_behavior_basic"), "振る舞いの基本", CardType.L_CARD)
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                })
                .build());

        // 4. 表情の基本 (Lカード): コスト体力1。元気+1、自分に集中+2付与。
        CardRegistry.register(CardDefinition.builder(id("card_facial_basic"), "表情の基本", CardType.L_CARD)
                .hpCost(1)
                .effect((player, deck) -> {
                    GenkiHelper.addGenki(player, 1f);
                    deck.getBuffState().addFocus(2);
                })
                .build());

        // 5. 静かな意志 (Oカード): 【要:上位進捗】コスト体力4。集中+3、好調2ターン(10秒)。初手確定。
        CardRegistry.register(CardDefinition.builder(id("card_quiet_will"), "静かな意志", CardType.O_CARD)
                .hpCost(4)
                .requiredAdvancement(UPPER_RANK_ADVANCEMENT)
                .guaranteedFirstDraw(true)
                .effect((player, deck) -> {
                    deck.getBuffState().addFocus(3);
                    deck.getBuffState().addGoodCondition(2 * DeckService.TICKS_PER_TURN, 2);
                })
                .build());

        // 6. 演出計画 (Oカード): 【要:上位進捗】コスト体力4。絶好調3ターン(15秒)付与。
        //    さらにデッキリセットまでの間、カード使用毎に固定で元気+2されるパッシブを付与。
        CardRegistry.register(CardDefinition.builder(id("card_direction_plan"), "演出計画", CardType.O_CARD)
                .hpCost(4)
                .requiredAdvancement(UPPER_RANK_ADVANCEMENT)
                .effect((player, deck) -> {
                    deck.getBuffState().addGreatCondition(3 * DeckService.TICKS_PER_TURN);
                    deck.getBuffState().setPassiveFlag("encore_genki_on_use", true);
                })
                .build());
    }
}
