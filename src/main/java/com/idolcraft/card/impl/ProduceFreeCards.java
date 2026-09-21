package com.idolcraft.card.impl;

import com.idolcraft.IdolCraft;
import com.idolcraft.buff.BuffState;
import com.idolcraft.card.CardCategory;
import com.idolcraft.card.CardDefinition;
import com.idolcraft.card.CardPlan;
import com.idolcraft.card.CardRegistry;
import com.idolcraft.card.CardType;
import com.idolcraft.util.GenkiHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * フリープランのスキルカード（本家学マス wiki 準拠。全9枚、issueコメント添付の F.json 準拠）。
 * フリープランのカードはどのプラン（センス／ロジック／アノマリー）のデッキにも組み込める。
 *
 * 効果は cost_base / effect_base（未強化状態）の数値を採用している（本Modはカード強化を実装していないため、
 * 既存のセンスプラン追加カードと同じ方針で base 値のみ実装）。
 * 「元気増加無効」「低下状態無効」「消費体力増加」「永続的な消費体力削減」等、本Modに厳密な対応概念が無い効果は
 * 近似・簡略化しており、各カードのコメントに注記する（センスプランと同じ方針）。
 */
public final class ProduceFreeCards {

    private ProduceFreeCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(IdolCraft.MOD_ID, path);
    }

    private static CardDefinition.Builder b(String path, String name, CardType type) {
        return CardDefinition.builder(id(path), name, type).plan(CardPlan.FREE);
    }

    /** 永続的な消費体力削減（「消費体力削減N」）は、消費体力減少の残ターンを大きな値にして近似する */
    private static final long PERMANENT_TURNS = 9999;

    /** 眠気（トラブルカード）：ロジック/アノマリーの一部カードが山札に生成する。手札に来ても何も起こらない。 */
    public static final ResourceLocation TROUBLE_DROWSINESS = id("card_drowsiness");

    public static void registerAll() {

        // 眠気: トラブルカード。パック抽選・作業台の習得候補には出さない（isTrouble）
        CardRegistry.register(b("card_drowsiness", "眠気", CardType.NORMAL)
                .description("トラブルカード。このカードを使用しても何も起こらない")
                .trouble(true)
                .effect((p, d) -> {})
                .build());

        // 気合十分！: R/M, コスト0, 元気+2, 消費体力減少2ターン, PLv16
        CardRegistry.register(b("card_free_fired_up", "気合十分！", CardType.NORMAL)
                .description("元気+2、消費体力減少2ターン")
                .category(CardCategory.MENTAL).requiredPLevel(16)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 2f);
                    d.getBuffState().addCostReductionTurns(2);
                }).build());

        // ファーストステップ: R/M, コスト0, 元気+3, 体力50%以上で消費体力削減1, PLv16, 重複不可/レッスン中1回
        CardRegistry.register(b("card_free_first_step", "ファーストステップ", CardType.ONCE_PER_LESSON)
                .description("元気+3、体力が50%以上の場合、消費体力削減1")
                .category(CardCategory.MENTAL).requiredPLevel(16)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 3f);
                    if (p.getHealth() >= p.getMaxHealth() * 0.5f) {
                        d.getBuffState().addCostReductionTurns(PERMANENT_TURNS);
                    }
                }).build());

        // 前途洋々: SR/A, コスト4, パラメータ+8 元気+7, PLv3
        CardRegistry.register(b("card_free_bright_future", "前途洋々", CardType.NORMAL)
                .description("パラメータ+8、元気+7")
                .hpCost(4).baseScore(8).requiredPLevel(3)
                .effect((p, d) -> {
                    dmg(p, d, 8);
                    GenkiHelper.addGenki(p, 7f);
                }).build());

        // アイドル宣言: SR/M, コスト1, スキルカード使用数追加+1 2枚引く, PLv23, 重複不可/レッスン中1回
        CardRegistry.register(b("card_free_idol_declaration", "アイドル宣言", CardType.ONCE_PER_LESSON)
                .description("スキルカード使用数追加+1、スキルカードを2枚引く")
                .hpCost(1).category(CardCategory.MENTAL).requiredPLevel(23)
                .effect((p, d) -> {
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(2);
                }).build());

        // ハイテンション: SR/M, コスト0, 元気+11 消費体力減少3ターン, PLv23, レッスン中1回
        // 近似: 「元気増加無効2ターン」は本Modに対応する仕組みが無いため省略
        CardRegistry.register(b("card_free_high_tension", "ハイテンション", CardType.ONCE_PER_LESSON)
                .description("元気+11、消費体力減少3ターン")
                .category(CardCategory.MENTAL).requiredPLevel(23)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 11f);
                    d.getBuffState().addCostReductionTurns(3);
                }).build());

        // テレビ出演: SSR/M, コスト1, 元気+3 消費体力減少4ターン, PLv5, 重複不可/レッスン中1回
        CardRegistry.register(b("card_free_tv_appearance", "テレビ出演", CardType.ONCE_PER_LESSON)
                .description("元気+3、消費体力減少4ターン")
                .hpCost(1).category(CardCategory.MENTAL).requiredPLevel(5)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 3f);
                    d.getBuffState().addCostReductionTurns(4);
                }).build());

        // かなえたい夢: SSR/M, コスト1, 元気+8 消費体力削減1(永続), PLv10, 重複不可/レッスン中1回
        CardRegistry.register(b("card_free_dream_to_fulfill", "かなえたい夢", CardType.ONCE_PER_LESSON)
                .description("元気+8、消費体力削減1")
                .hpCost(1).category(CardCategory.MENTAL).requiredPLevel(10)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 8f);
                    d.getBuffState().addCostReductionTurns(PERMANENT_TURNS);
                }).build());

        // アイドル魂: SSR/M, コスト2, レッスン開始時手札に入る 元気+6 スキルカード使用数追加+1, PLv35, 重複不可/レッスン中1回
        // 近似: 「低下状態無効(1回)」は対応する負の状態が本Modに存在しないため省略
        CardRegistry.register(b("card_free_idol_spirit", "アイドル魂", CardType.ONCE_PER_LESSON)
                .description("レッスン開始時手札に入る、元気+6、スキルカード使用数追加+1")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(35).guaranteedFirstDraw(true)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 6f);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // 仕切り直し: SSR/M, コスト5, 手札をすべて入れ替える 消費体力減少4ターン スキルカード使用数追加+1 2枚引く, PLv40, 重複不可/レッスン中1回
        // 近似: DeckService は発動直後に使用済みカードを手札からインデックスで除去するため、
        // 効果内で手札を並び替える／削除するのは事故（IndexOutOfBounds）の元になり安全に実装できない。
        // 「手札をすべて入れ替える」は省略し、残りの効果（消費体力減少・追加使用・追加ドロー）のみ実装する。
        CardRegistry.register(b("card_free_fresh_start", "仕切り直し", CardType.ONCE_PER_LESSON)
                .description("消費体力減少4ターン、スキルカード使用数追加+1、スキルカードを2枚引く")
                .hpCost(5).category(CardCategory.MENTAL).requiredPLevel(40)
                .effect((p, d) -> {
                    d.getBuffState().addCostReductionTurns(4);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(2);
                }).build());
    }

    private static void dmg(net.minecraft.server.level.ServerPlayer player, com.idolcraft.capability.IDeckData deck, long base) {
        net.minecraft.world.entity.LivingEntity t = com.idolcraft.util.TargetingHelper.getLookTarget(player);
        if (t != null) {
            t.hurt(player.level().damageSources().magic(), com.idolcraft.util.ScoreMath.calculateDamage(base, deck.getBuffState()));
        }
    }
}
