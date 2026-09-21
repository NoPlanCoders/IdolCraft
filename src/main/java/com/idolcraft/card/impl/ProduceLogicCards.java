package com.idolcraft.card.impl;

import com.idolcraft.IdolCraft;
import com.idolcraft.buff.BuffState;
import com.idolcraft.capability.DeckService;
import com.idolcraft.capability.IDeckData;
import com.idolcraft.card.CardCategory;
import com.idolcraft.card.CardDefinition;
import com.idolcraft.card.CardPlan;
import com.idolcraft.card.CardRegistry;
import com.idolcraft.card.CardType;
import com.idolcraft.util.GenkiHelper;
import com.idolcraft.util.ScoreMath;
import com.idolcraft.util.TargetingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * ロジックプランのスキルカード（issueコメント添付の Rcard.json 準拠、全52枚）。
 *
 * ロジックプラン固有の新規メカニクス：
 * - やる気／好印象：集中と同じ永続スタック型バフ（{@link BuffState#getMotivation()} / {@link BuffState#getGoodImpression()}）。
 * - 「元気の110%分パラメータ上昇」等：対象ステータスの現在値に割合を掛けてパラメータ上昇量とする（本家式をそのまま採用）。
 * - 「除外以外にあるトラブルカードが2枚以上の場合」：山札・手札・捨て札にある「眠気」等のトラブルカードの枚数で判定する。
 * - 「好印象強化+X%」：一定ターンの間、好印象の増加量を割合で上乗せする一時バフ。
 *
 * 効果は cost_base / effect_base（未強化状態）を採用（本Modはカード強化未実装のため、既存センスプランと同方針）。
 * 「消費体力増加」（本Modでは対応する仕組みが無く既存カードでも実質no-op）、「ターン追加」（スキルカード追加使用で近似）、
 * 一部の複合的な永続バフ（「夢色リップ」の好印象倍率など）は近似・簡略化しており、各カードのコメントに注記する。
 */
public final class ProduceLogicCards {

    private ProduceLogicCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(IdolCraft.MOD_ID, path);
    }

    private static CardDefinition.Builder b(String path, String name, CardType type) {
        return CardDefinition.builder(id(path), name, type).plan(CardPlan.LOGIC);
    }

    // ── 効果ヘルパー ──
    private static void dmg(ServerPlayer p, IDeckData d, long base) {
        LivingEntity t = TargetingHelper.getLookTarget(p);
        if (t != null) t.hurt(p.level().damageSources().magic(), ScoreMath.calculateDamage(base, d.getBuffState()));
    }
    private static long pctOfGenki(ServerPlayer p, double pct) { return (long) Math.ceil(GenkiHelper.getGenki(p) * pct / 100.0); }
    private static long pctOfMotivation(IDeckData d, double pct) { return (long) Math.ceil(d.getBuffState().getMotivation() * pct / 100.0); }
    private static long pctOfImpression(IDeckData d, double pct) { return (long) Math.ceil(d.getBuffState().getGoodImpression() * pct / 100.0); }
    /** 好印象を得る（「好印象強化」の一時バフがあれば割合分だけ上乗せする） */
    private static void gainImpression(IDeckData d, long base) {
        BuffState b = d.getBuffState();
        long boosted = base + (long) Math.ceil(base * b.getImpressionBoostPercent() / 100.0);
        b.addGoodImpression(boosted);
    }
    private static boolean troubleAtLeast(IDeckData d, long n) { return DeckService.countTroubleCardsOutsideExclusion(d) >= n; }
    private static void spawnDrowsiness(IDeckData d) { DeckService.insertTroubleCard(d, ProduceFreeCards.TROUBLE_DROWSINESS); }

    public static void registerAll() {

        // ============ R（銀） ============

        // 今日もおはよう: A, コスト4, パラメータ+7 好印象+3
        CardRegistry.register(b("card_logic_good_morning", "今日もおはよう", CardType.NORMAL)
                .description("パラメータ+7、好印象+3").hpCost(4).baseScore(7)
                .effect((p, d) -> { dmg(p, d, 7); gainImpression(d, 3); }).build());

        // ゆるふわおしゃべり: A, コスト4, やる気+3 元気の60%分パラメータ上昇, レッスン中1回
        CardRegistry.register(b("card_logic_easy_chat", "ゆるふわおしゃべり", CardType.ONCE_PER_LESSON)
                .description("やる気+3、元気の60%分パラメータ上昇").hpCost(4)
                .effect((p, d) -> { d.getBuffState().addMotivation(3); dmg(p, d, pctOfGenki(p, 60)); }).build());

        // もう少しだけ: A, コスト5, パラメータ+10 やる気+3
        CardRegistry.register(b("card_logic_just_a_bit_more", "もう少しだけ", CardType.NORMAL)
                .description("パラメータ+10、やる気+3").hpCost(5).baseScore(10)
                .effect((p, d) -> { dmg(p, d, 10); d.getBuffState().addMotivation(3); }).build());

        // 手拍子: A, コスト5, 好印象の150%分パラメータ上昇, PLv13, レッスン中1回
        CardRegistry.register(b("card_logic_clapping", "手拍子", CardType.ONCE_PER_LESSON)
                .description("好印象の150%分パラメータ上昇").hpCost(5).requiredPLevel(13)
                .effect((p, d) -> dmg(p, d, pctOfImpression(d, 150))).build());

        // 元気な挨拶: A, コスト4, 元気の110%分パラメータ上昇, PLv14, レッスン中1回
        CardRegistry.register(b("card_logic_energetic_greeting", "元気な挨拶", CardType.ONCE_PER_LESSON)
                .description("元気の110%分パラメータ上昇").hpCost(4).requiredPLevel(14)
                .effect((p, d) -> dmg(p, d, pctOfGenki(p, 110))).build());

        // おまもりミラクル: A, コスト3, 好印象+2 好印象の60%分パラメータ上昇 (トラブル2枚以上で好印象の40%分追加), PLv51, レッスン中1回
        CardRegistry.register(b("card_logic_lucky_charm", "おまもりミラクル", CardType.ONCE_PER_LESSON)
                .description("好印象+2、好印象の60%分パラメータ上昇（トラブルカード2枚以上でさらに40%分）")
                .hpCost(3).requiredPLevel(51)
                .effect((p, d) -> {
                    gainImpression(d, 2);
                    long pct = troubleAtLeast(d, 2) ? 100 : 60;
                    dmg(p, d, pctOfImpression(d, pct));
                }).build());

        // がむしゃら: A, コスト2, 元気+2 やる気+2 (トラブル2枚以上で元気の50%分パラメータ上昇), PLv52, レッスン中1回
        CardRegistry.register(b("card_logic_reckless", "がむしゃら", CardType.ONCE_PER_LESSON)
                .description("元気+2、やる気+2（トラブルカード2枚以上で元気の50%分パラメータ上昇）")
                .hpCost(2).requiredPLevel(52)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 2f);
                    d.getBuffState().addMotivation(2);
                    if (troubleAtLeast(d, 2)) dmg(p, d, pctOfGenki(p, 50));
                }).build());

        // デイドリーミング: A, コスト3, 条件:元気30以上, 元気+3 以降3ターン、ターン開始時、元気の60%分パラメータ上昇, PLv62, 重複不可/レッスン中1回
        // 近似: 「ターン開始時」の継続効果は、本Modの汎用「継続パラメータ」機構（ターン終了時に固定値ダメージ）で代用する。
        CardRegistry.register(b("card_logic_daydreaming", "デイドリーミング", CardType.ONCE_PER_LESSON)
                .description("元気+3、以降3ターンの間、ターン終了時に元気の60%分パラメータ上昇（近似）")
                .hpCost(3).requiredPLevel(62)
                .usableWhen((pl, d) -> GenkiHelper.getGenki(pl) >= 30, "元気30以上が必要")
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 3f);
                    d.getBuffState().addParamPerTurn(pctOfGenki(p, 60));
                }).build());

        // ============ R（銀） メンタル ============

        // リスタート: M, コスト4, 元気+2 好印象+3
        CardRegistry.register(b("card_logic_restart", "リスタート", CardType.NORMAL)
                .description("元気+2、好印象+3").hpCost(4).category(CardCategory.MENTAL)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 2f); gainImpression(d, 3); }).build());

        // えいえいおー: M, コスト2, 元気+1 やる気+3
        CardRegistry.register(b("card_logic_ei_ei_oh", "えいえいおー", CardType.NORMAL)
                .description("元気+1、やる気+3").hpCost(2).category(CardCategory.MENTAL)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 1f); d.getBuffState().addMotivation(3); }).build());

        // リズミカル: M, コスト0, 元気+6, PLv2, レッスン中1回
        CardRegistry.register(b("card_logic_rhythmical", "リズミカル", CardType.ONCE_PER_LESSON)
                .description("元気+6").category(CardCategory.MENTAL).requiredPLevel(2)
                .effect((p, d) -> GenkiHelper.addGenki(p, 6f)).build());

        // 思い出し笑い: M, コスト2, 好印象+3 (好印象3以上でやる気+2), PLv4
        CardRegistry.register(b("card_logic_fond_memory_laugh", "思い出し笑い", CardType.NORMAL)
                .description("好印象+3、好印象が3以上の場合、やる気+2")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(4)
                .effect((p, d) -> {
                    gainImpression(d, 3);
                    if (d.getBuffState().getGoodImpression() >= 3) d.getBuffState().addMotivation(2);
                }).build());

        // パステル気分: M, コスト3, 元気+5 (やる気3以上で好印象+3), PLv9
        CardRegistry.register(b("card_logic_pastel_mood", "パステル気分", CardType.NORMAL)
                .description("元気+5、やる気が3以上の場合、好印象+3")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(9)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 5f);
                    if (d.getBuffState().getMotivation() >= 3) gainImpression(d, 3);
                }).build());

        // 励まし: M, コスト4, やる気+3 (やる気6以上で好印象+4), PLv19
        CardRegistry.register(b("card_logic_encouragement", "励まし", CardType.NORMAL)
                .description("やる気+3、やる気が6以上の場合、好印象+4")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(19)
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(3);
                    if (d.getBuffState().getMotivation() >= 6) gainImpression(d, 4);
                }).build());

        // 幸せのおまじない: M, コスト8, 好印象+7, PLv46, レッスン中1回
        CardRegistry.register(b("card_logic_happy_charm", "幸せのおまじない", CardType.ONCE_PER_LESSON)
                .description("好印象+7").hpCost(8).category(CardCategory.MENTAL).requiredPLevel(46)
                .effect((p, d) -> gainImpression(d, 7)).build());

        // イメチェン: M, コスト"やる気1", 好印象+4 好印象強化+10%, PLv72, レッスン中1回
        CardRegistry.register(b("card_logic_makeover", "イメチェン", CardType.ONCE_PER_LESSON)
                .description("やる気1消費、好印象+4、好印象強化+10%")
                .category(CardCategory.MENTAL).requiredPLevel(72)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 1, "やる気1以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-1);
                    gainImpression(d, 4);
                    d.getBuffState().addImpressionBoost(10, 9999);
                }).build());

        // ============ SR（金） アクティブ ============

        // ラブリーウィンク: A, コスト5, 好印象+4 好印象の60%分パラメータ上昇
        CardRegistry.register(b("card_logic_lovely_wink", "ラブリーウィンク", CardType.NORMAL)
                .description("好印象+4、好印象の60%分パラメータ上昇").hpCost(5)
                .effect((p, d) -> { gainImpression(d, 4); dmg(p, d, pctOfImpression(d, 60)); }).build());

        // ありがとうの言葉: A, コスト3, 元気+9 元気の40%分パラメータ上昇, レッスン中1回
        CardRegistry.register(b("card_logic_words_of_thanks", "ありがとうの言葉", CardType.ONCE_PER_LESSON)
                .description("元気+9、元気の40%分パラメータ上昇").hpCost(3)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 9f); dmg(p, d, pctOfGenki(p, 40)); }).build());

        // ハートの合図: A, コスト3, 元気の130%分パラメータ上昇させ、元気を半分にする, PLv6, レッスン中1回
        CardRegistry.register(b("card_logic_heart_signal", "ハートの合図", CardType.ONCE_PER_LESSON)
                .description("元気の130%分パラメータ上昇させ、元気を半分にする").hpCost(3).requiredPLevel(6)
                .effect((p, d) -> {
                    dmg(p, d, pctOfGenki(p, 130));
                    p.setAbsorptionAmount(GenkiHelper.getGenki(p) * 0.5f);
                }).build());

        // キラメキ: A, コスト3, 元気+5 好印象の200%分パラメータ上昇, PLv24, 重複不可/レッスン中1回
        // 近似: 消費体力増加2ターンは本Mod未対応のため省略
        CardRegistry.register(b("card_logic_sparkle", "キラメキ", CardType.ONCE_PER_LESSON)
                .description("元気+5、好印象の200%分パラメータ上昇").hpCost(3).requiredPLevel(24)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 5f); dmg(p, d, pctOfImpression(d, 200)); }).build());

        // みんな大好き: A, コスト"やる気3", 好印象の90%分パラメータ上昇 スキルカード使用数追加+1, PLv34, 重複不可
        CardRegistry.register(b("card_logic_everyone_loves", "みんな大好き", CardType.NORMAL)
                .description("やる気3消費、好印象の90%分パラメータ上昇、スキルカード使用数追加+1")
                .requiredPLevel(34)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 3, "やる気3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-3);
                    dmg(p, d, pctOfImpression(d, 90));
                    d.getBuffState().addBonusAction(1);
                }).build());

        // きらきら紙吹雪: A, コスト"やる気3", 元気の110%分パラメータ上昇 次ターン、スキルカードを引く, PLv57, 重複不可
        CardRegistry.register(b("card_logic_glitter_confetti", "きらきら紙吹雪", CardType.NORMAL)
                .description("やる気3消費、元気の110%分パラメータ上昇、スキルカードを引く")
                .requiredPLevel(57)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 3, "やる気3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-3);
                    dmg(p, d, pctOfGenki(p, 110));
                    d.getBuffState().addPendingDraw(1);
                }).build());

        // ============ SR（金） メンタル ============

        // あふれる思い出: M, コスト3, 元気+2 やる気+4
        CardRegistry.register(b("card_logic_overflowing_memories", "あふれる思い出", CardType.NORMAL)
                .description("元気+2、やる気+4").hpCost(3).category(CardCategory.MENTAL)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 2f); d.getBuffState().addMotivation(4); }).build());

        // ふれあい: M, コスト5, 元気+2 好印象+4
        CardRegistry.register(b("card_logic_bonding", "ふれあい", CardType.NORMAL)
                .description("元気+2、好印象+4").hpCost(5).category(CardCategory.MENTAL)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 2f); gainImpression(d, 4); }).build());

        // 幸せな時間: M, コスト5, 好印象+6, PLv7
        CardRegistry.register(b("card_logic_happy_time", "幸せな時間", CardType.NORMAL)
                .description("好印象+6").hpCost(5).category(CardCategory.MENTAL).requiredPLevel(7)
                .effect((p, d) -> gainImpression(d, 6)).build());

        // ファンシーチャーム: M, コスト4, 好印象+3 以降、メンタルカード使用時好印象+1, PLv17, 重複不可/レッスン中1回
        CardRegistry.register(b("card_logic_fancy_charm", "ファンシーチャーム", CardType.ONCE_PER_LESSON)
                .description("好印象+3、以降メンタルスキルカード使用時、好印象+1（重複可）")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(17)
                .effect((p, d) -> { gainImpression(d, 3); d.getBuffState().addOnMentalUseImpression(1); }).build());

        // ワクワクが止まらない: M, コスト3, やる気+3 以降、メンタルカード使用時やる気+1, PLv18, 重複不可/レッスン中1回
        CardRegistry.register(b("card_logic_cant_stop_excitement", "ワクワクが止まらない", CardType.ONCE_PER_LESSON)
                .description("やる気+3、以降メンタルスキルカード使用時、やる気+1（重複可）")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(18)
                .effect((p, d) -> { d.getBuffState().addMotivation(3); d.getBuffState().addOnMentalUseMotivation(1); }).build());

        // 本番前夜: M, コスト5, レッスン開始時手札に入る 好印象+4 やる気+3, PLv20, レッスン中1回
        CardRegistry.register(b("card_logic_night_before", "本番前夜", CardType.ONCE_PER_LESSON)
                .description("レッスン開始時手札に入る、好印象+4、やる気+3")
                .hpCost(5).category(CardCategory.MENTAL).requiredPLevel(20).guaranteedFirstDraw(true)
                .effect((p, d) -> { gainImpression(d, 4); d.getBuffState().addMotivation(3); }).build());

        // ひなたぼっこ: M, コスト0, 元気+11 やる気+5, PLv22, 重複不可/レッスン中1回 (消費体力増加は近似省略)
        CardRegistry.register(b("card_logic_sunbathing", "ひなたぼっこ", CardType.ONCE_PER_LESSON)
                .description("元気+11、やる気+5").category(CardCategory.MENTAL).requiredPLevel(22)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 11f); d.getBuffState().addMotivation(5); }).build());

        // イメトレ: M, コスト4, 元気+7 やる気+4, PLv28, レッスン中1回
        CardRegistry.register(b("card_logic_image_training", "イメトレ", CardType.ONCE_PER_LESSON)
                .description("元気+7、やる気+4").hpCost(4).category(CardCategory.MENTAL).requiredPLevel(28)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 7f); d.getBuffState().addMotivation(4); }).build());

        // やる気は満点: M, コスト1, 元気+1 好印象+4, PLv29, レッスン中1回
        CardRegistry.register(b("card_logic_full_of_motivation", "やる気は満点", CardType.ONCE_PER_LESSON)
                .description("元気+1、好印象+4").hpCost(1).category(CardCategory.MENTAL).requiredPLevel(29)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 1f); gainImpression(d, 4); }).build());

        // ゆめみごこち: M, コスト"好印象2", やる気+4 スキルカード使用数追加+1, PLv32, 重複不可
        CardRegistry.register(b("card_logic_dreamy_mood", "ゆめみごこち", CardType.NORMAL)
                .description("好印象2消費、やる気+4、スキルカード使用数追加+1")
                .category(CardCategory.MENTAL).requiredPLevel(32)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 2, "好印象2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addGoodImpression(-2);
                    d.getBuffState().addMotivation(4);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // 止められない想い: M, コスト4, やる気+3 好印象+3 スキルカード使用数追加+1, PLv48, 重複不可
        CardRegistry.register(b("card_logic_unstoppable_feelings", "止められない想い", CardType.NORMAL)
                .description("やる気+3、好印象+3、スキルカード使用数追加+1")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(48)
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(3);
                    gainImpression(d, 3);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // オトメゴコロ: M, コスト"やる気3", 好印象+4 スキルカード使用数追加+1, PLv52, 重複不可
        CardRegistry.register(b("card_logic_maiden_heart", "オトメゴコロ", CardType.NORMAL)
                .description("やる気3消費、好印象+4、スキルカード使用数追加+1")
                .category(CardCategory.MENTAL).requiredPLevel(52)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 3, "やる気3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-3);
                    gainImpression(d, 4);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // 冒険心: M, コスト3, 好印象+6 スキルカード使用数追加+1 スキルカードを引く 眠気生成, PLv53, 重複不可/レッスン中1回
        CardRegistry.register(b("card_logic_adventurous_spirit", "冒険心", CardType.ONCE_PER_LESSON)
                .description("好印象+6、スキルカード使用数追加+1、スキルカードを引く、眠気を山札に生成")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(53)
                .effect((p, d) -> {
                    gainImpression(d, 6);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(1);
                    spawnDrowsiness(d);
                }).build());

        // 気まぐれハート: M, コスト2, 元気+3 やる気+5 スキルカード使用数追加+1 スキルカードを引く 眠気生成, PLv54, 重複不可/レッスン中1回
        CardRegistry.register(b("card_logic_capricious_heart", "気まぐれハート", CardType.ONCE_PER_LESSON)
                .description("元気+3、やる気+5、スキルカード使用数追加+1、スキルカードを引く、眠気を山札に生成")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(54)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 3f);
                    d.getBuffState().addMotivation(5);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(1);
                    spawnDrowsiness(d);
                }).build());

        // 成長痛: M, コスト5, 元気+2 やる気+3 好印象+4 スキルカード使用数追加+1 眠気生成, PLv57, 重複不可
        CardRegistry.register(b("card_logic_growing_pains", "成長痛", CardType.NORMAL)
                .description("元気+2、やる気+3、好印象+4、スキルカード使用数追加+1、眠気を山札に生成")
                .hpCost(5).category(CardCategory.MENTAL).requiredPLevel(57)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 2f);
                    d.getBuffState().addMotivation(3);
                    gainImpression(d, 4);
                    d.getBuffState().addBonusAction(1);
                    spawnDrowsiness(d);
                }).build());

        // ============ SSR（虹） アクティブ ============

        // 200%スマイル: A, コスト6, 好印象+5 好印象の100%分パラメータ上昇, PLv11, 重複不可/レッスン中1回
        CardRegistry.register(b("card_logic_200_smile", "200%スマイル", CardType.ONCE_PER_LESSON)
                .description("好印象+5、好印象の100%分パラメータ上昇").hpCost(6).requiredPLevel(11)
                .effect((p, d) -> { gainImpression(d, 5); dmg(p, d, pctOfImpression(d, 100)); }).build());

        // 開花: A, コスト5, やる気+6 やる気の200%分パラメータ上昇, PLv12, 重複不可
        CardRegistry.register(b("card_logic_blooming", "開花", CardType.NORMAL)
                .description("やる気+6、やる気の200%分パラメータ上昇").hpCost(5).requiredPLevel(12)
                .effect((p, d) -> { d.getBuffState().addMotivation(6); dmg(p, d, pctOfMotivation(d, 200)); }).build());

        // 届いて!: A, コスト1, 条件:元気7以上, 元気の320%分パラメータ上昇させ、元気を0にする, PLv44, 重複不可
        CardRegistry.register(b("card_logic_reach_you", "届いて!", CardType.NORMAL)
                .description("元気の320%分パラメータ上昇させ、元気を0にする").hpCost(1).requiredPLevel(44)
                .usableWhen((pl, d) -> GenkiHelper.getGenki(pl) >= 7, "元気7以上が必要")
                .effect((p, d) -> {
                    dmg(p, d, pctOfGenki(p, 320));
                    p.setAbsorptionAmount(0f);
                }).build());

        // 輝くキミへ: A, コスト"やる気4", スキルカード使用数追加+1 以降スキルカード使用時好印象の30%分パラメータ上昇, PLv50, 重複不可
        // 近似: 「以降」の永続パッシブは、成長カウンタを介さず単純化し、発動時点の好印象を1回だけ加点するに留める。
        CardRegistry.register(b("card_logic_shine_for_you", "輝くキミへ", CardType.NORMAL)
                .description("やる気4消費、スキルカード使用数追加+1、好印象の30%分パラメータ上昇")
                .requiredPLevel(50)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 4, "やる気4以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-4);
                    d.getBuffState().addBonusAction(1);
                    dmg(p, d, pctOfImpression(d, 30));
                }).build());

        // あのときの約束: A, コスト"好印象3", 条件:やる気3以上, 元気+14 元気の140%分パラメータ上昇 やる気の200%分パラメータ上昇, PLv54, 重複不可
        CardRegistry.register(b("card_logic_that_promise", "あのときの約束", CardType.NORMAL)
                .description("好印象3消費、元気+14、元気の140%分パラメータ上昇、やる気の200%分パラメータ上昇")
                .requiredPLevel(54)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 3 && d.getBuffState().getMotivation() >= 3,
                        "好印象3以上・やる気3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addGoodImpression(-3);
                    GenkiHelper.addGenki(p, 14f);
                    dmg(p, d, pctOfGenki(p, 140));
                    dmg(p, d, pctOfMotivation(d, 200));
                }).build());

        // キセキの魔法: A, コスト4, 条件:好印象10以上, 好印象の400%分パラメータ上昇 (トラブル2枚以上でさらに400%分), PLv59, 重複不可
        CardRegistry.register(b("card_logic_miracle_magic", "キセキの魔法", CardType.NORMAL)
                .description("好印象の400%分パラメータ上昇（トラブルカード2枚以上でさらに400%分）")
                .hpCost(4).requiredPLevel(59)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 10, "好印象10以上が必要")
                .effect((p, d) -> {
                    dmg(p, d, pctOfImpression(d, 400));
                    if (troubleAtLeast(d, 2)) dmg(p, d, pctOfImpression(d, 400));
                }).build());

        // せのびの魔法: A, コスト3, 以降3ターン、ターン開始時、元気+2 4ターン後、元気の180%分パラメータ上昇, PLv76, 重複不可
        // 近似: 「4ターン後にまとめてパラメータ上昇」は、継続パラメータ機構でターン終了時に少しずつ加点する形に単純化する。
        CardRegistry.register(b("card_logic_growth_spurt_magic", "せのびの魔法", CardType.NORMAL)
                .description("以降のターン終了時、元気+2かつパラメータ上昇（近似）").hpCost(3).requiredPLevel(76)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 2f);
                    d.getBuffState().addParamPerTurn(pctOfGenki(p, 45));
                }).build());

        // びしっとキメ顔: A, コスト5, 条件:好印象10以上, 好印象強化+200%(3ターン) 以降3ターン好印象増加後好印象の100%分パラメータ上昇, PLv79, 重複不可
        // 近似: 「直接効果で好印象増加後」の即時追撃は複雑なため、好印象強化バフの付与のみ実装する。
        CardRegistry.register(b("card_logic_perfect_pose_face", "びしっとキメ顔", CardType.NORMAL)
                .description("好印象強化+200%（3ターン）").hpCost(5).requiredPLevel(79)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 10, "好印象10以上が必要")
                .effect((p, d) -> d.getBuffState().addImpressionBoost(200, 3)).build());

        // ============ SSR（虹） メンタル ============

        // 私がスター: M, コスト"好印象2", ターン追加+1 スキルカード使用数追加+1, PLv25, 重複不可
        // 近似: 「ターン追加+1」は本Modのターン構造上「スキルカード追加使用+1」で代用する。
        CardRegistry.register(b("card_logic_im_the_star", "私がスター", CardType.NORMAL)
                .description("好印象2消費、スキルカード使用数追加+2（ターン追加の近似）")
                .category(CardCategory.MENTAL).requiredPLevel(25)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 2, "好印象2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addGoodImpression(-2);
                    d.getBuffState().addBonusAction(2);
                }).build());

        // 星屑センセーション: M, コスト"やる気3", 好印象+5 スキルカード使用数追加+1 (好印象10以上で好印象増加量増加+50%(5ターン)), PLv30, 重複不可
        CardRegistry.register(b("card_logic_stardust_sensation", "星屑センセーション", CardType.NORMAL)
                .description("やる気3消費、好印象+5、スキルカード使用数追加+1、好印象10以上で好印象強化+50%(5ターン)")
                .category(CardCategory.MENTAL).requiredPLevel(30)
                .usableWhen((pl, d) -> d.getBuffState().getMotivation() >= 3, "やる気3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addMotivation(-3);
                    gainImpression(d, 5);
                    d.getBuffState().addBonusAction(1);
                    if (d.getBuffState().getGoodImpression() >= 10) d.getBuffState().addImpressionBoost(50, 5);
                }).build());

        // ノートの端の決意: M, コスト4, 好印象+3 やる気+3 消費体力減少3ターン, PLv37, 重複不可
        CardRegistry.register(b("card_logic_notebook_resolve", "ノートの端の決意", CardType.NORMAL)
                .description("好印象+3、やる気+3、消費体力減少3ターン")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(37)
                .effect((p, d) -> {
                    gainImpression(d, 3);
                    d.getBuffState().addMotivation(3);
                    d.getBuffState().addCostReductionTurns(3);
                }).build());

        // 手書きのメッセージ: M, コスト"好印象2", 元気+9 元気+9, PLv39, 重複不可
        CardRegistry.register(b("card_logic_handwritten_message", "手書きのメッセージ", CardType.NORMAL)
                .description("好印象2消費、元気+18")
                .category(CardCategory.MENTAL).requiredPLevel(39)
                .usableWhen((pl, d) -> d.getBuffState().getGoodImpression() >= 2, "好印象2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addGoodImpression(-2);
                    GenkiHelper.addGenki(p, 18f);
                }).build());

        // トキメキ: M, コスト10, 好印象+8 やる気+5, PLv42, 重複不可
        CardRegistry.register(b("card_logic_heart_flutter", "トキメキ", CardType.NORMAL)
                .description("好印象+8、やる気+5").hpCost(10).category(CardCategory.MENTAL).requiredPLevel(42)
                .effect((p, d) -> { gainImpression(d, 8); d.getBuffState().addMotivation(5); }).build());

        // 虹色ドリーマー: M, コスト9, 好印象+1 以降ターン終了時好印象3以上で好印象+3, PLv45, 重複不可
        CardRegistry.register(b("card_logic_rainbow_dreamer", "虹色ドリーマー", CardType.NORMAL)
                .description("好印象+1、以降ターン終了時、好印象が3以上の場合、好印象+3（重複可）")
                .hpCost(9).category(CardCategory.MENTAL).requiredPLevel(45)
                .effect((p, d) -> {
                    gainImpression(d, 1);
                    d.getBuffState().setCustomCounter("impression_per_turn_stacks",
                            d.getBuffState().getCustomCounter("impression_per_turn_stacks") + 1);
                }).build());

        // 夢色リップ: M, コスト4, 元気+2 やる気+4, PLv64, 重複不可
        // 近似: 「パラメータ上昇量増加10%」「好印象15以上で以降3ターン好印象1.1倍」は複合的すぎるため省略する。
        CardRegistry.register(b("card_logic_dreamy_lip", "夢色リップ", CardType.NORMAL)
                .description("元気+2、やる気+4").hpCost(4).category(CardCategory.MENTAL).requiredPLevel(64)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 2f); d.getBuffState().addMotivation(4); }).build());
    }
}
