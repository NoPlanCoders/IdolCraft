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
 * アノマリープランのスキルカード（issueコメント添付の Acard.json 準拠、全51枚）。
 *
 * アノマリープラン固有の新規メカニクス：
 * - 全力値：スタック型リソース。コスト（「全力値N」）としても消費される（{@link BuffState#getZenryoku()}）。
 * - 強気／温存：段階型ステータス（0=無し/1/2段階目、{@link BuffState#getBoldStage()} / {@link BuffState#getConserveStage()}）。
 * - 指針：本Modでは強気・温存のいずれかが有効な状態をまとめて指針として扱う近似（{@link BuffState#hasAnyPolicy()}）。
 * - 熱意：一定ターンの間パラメータ上昇量を割合で上乗せする一時バフ。
 *
 * 効果は cost_base / effect_base（未強化状態）を採用。
 * 以下は本Modに厳密な対応概念が無いため近似・簡略化している（各カードのコメントにも個別注記）：
 * 「自身／手札のカードを保留に移動」（保留ゾーン自体が未実装のため当該clauseは省略）、
 * 「次のターン、○○に変更」（本Modでは即時適用に単純化）、
 * 「アクティブ／メンタルスキルカードのコスト値増加」（コストは静的なため増加分のみ省略、パラメータ側の増加は実装）、
 * 「成長」（自己参照的な累積は複雑なため、多くのカードで対象カードの重複使用そのものが少なく効果が薄いことから省略、
 * 一部のみ簡易な近似で実装）。
 */
public final class ProduceAnomalyCards {

    private ProduceAnomalyCards() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(IdolCraft.MOD_ID, path);
    }

    private static CardDefinition.Builder b(String path, String name, CardType type) {
        return CardDefinition.builder(id(path), name, type).plan(CardPlan.ANOMALY);
    }

    // ── 効果ヘルパー ──
    /** パラメータ上昇（「アクティブスキルカードのパラメータ値増加」の永続バフ・熱意の一時バフを両方適用する） */
    private static void dmg(ServerPlayer p, IDeckData d, long base) {
        BuffState buff = d.getBuffState();
        long boosted = base + buff.getActiveParamBonus();
        boosted += (long) Math.ceil(boosted * buff.getEnthusiasmPercent() / 100.0);
        LivingEntity t = TargetingHelper.getLookTarget(p);
        if (t != null) t.hurt(p.level().damageSources().magic(), ScoreMath.calculateDamage(boosted, buff));
    }
    private static void dmgTimes(ServerPlayer p, IDeckData d, long base, int times) {
        for (int i = 0; i < times; i++) dmg(p, d, base);
    }
    private static long pctOfZenryokuAccumulated(IDeckData d, double pct) {
        return (long) Math.floor(d.getBuffState().getZenryokuAccumulated() * pct / 100.0);
    }
    private static void setBold(IDeckData d, long stage) { d.getBuffState().setBoldStageAtLeast(stage); }
    private static void setConserve(IDeckData d, long stage) {
        d.getBuffState().setConserveStageAtLeast(stage);
        d.getBuffState().incrementConserveActivations();
    }
    private static void spawnDrowsiness(IDeckData d) { DeckService.insertTroubleCard(d, ProduceFreeCards.TROUBLE_DROWSINESS); }
    private static boolean troubleAtLeast(IDeckData d, long n) { return DeckService.countTroubleCardsOutsideExclusion(d) >= n; }

    public static void registerAll() {

        // ============ R（銀） アクティブ ============

        // ジャストアピール: コスト7, パラメータ+15 全力値+2, レッスン中1回
        CardRegistry.register(b("card_anomaly_just_appeal", "ジャストアピール", CardType.ONCE_PER_LESSON)
                .description("パラメータ+15、全力値+2").hpCost(7).baseScore(15)
                .effect((p, d) -> { dmg(p, d, 15); d.getBuffState().addZenryoku(2); }).build());

        // スターライト: コスト6, 強気に変更 パラメータ+9
        CardRegistry.register(b("card_anomaly_starlight", "スターライト", CardType.NORMAL)
                .description("強気に変更、パラメータ+9").hpCost(6).baseScore(9)
                .effect((p, d) -> { setBold(d, 1); dmg(p, d, 9); }).build());

        // 一歩: コスト4, パラメータ+15, PLv2, レッスン中1回
        CardRegistry.register(b("card_anomaly_one_step", "一歩", CardType.ONCE_PER_LESSON)
                .description("パラメータ+15").hpCost(4).baseScore(15).requiredPLevel(2)
                .effect((p, d) -> dmg(p, d, 15)).build());

        // ラッキー♪: コスト3, パラメータ+8 強気の場合、全力値+3, PLv9, レッスン中1回
        CardRegistry.register(b("card_anomaly_lucky", "ラッキー♪", CardType.ONCE_PER_LESSON)
                .description("パラメータ+8、強気の場合、全力値+3").hpCost(3).baseScore(8).requiredPLevel(9)
                .effect((p, d) -> {
                    dmg(p, d, 8);
                    if (d.getBuffState().isBold()) d.getBuffState().addZenryoku(3);
                }).build());

        // 積み重ね: コスト3, 全力値+2 全力の場合、パラメータ+20, PLv13
        // 近似: 「温存の場合、自身を保留に移動」は保留ゾーン未実装のため省略
        CardRegistry.register(b("card_anomaly_steady_stack", "積み重ね", CardType.NORMAL)
                .description("全力値+2、全力の場合、パラメータ+20").hpCost(3).requiredPLevel(13)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(2);
                    if (d.getBuffState().getZenryoku() > 0) dmg(p, d, 20);
                }).build());

        // 精一杯: コスト6, 強気に変更 パラメータ+6（2回）, PLv14, レッスン中1回
        CardRegistry.register(b("card_anomaly_all_out", "精一杯", CardType.ONCE_PER_LESSON)
                .description("強気に変更、パラメータ+6（2回）").hpCost(6).baseScore(12).requiredPLevel(14)
                .effect((p, d) -> { setBold(d, 1); dmgTimes(p, d, 6, 2); }).build());

        // 形成逆転: コスト4, 強気に変更 パラメータ+9 [トラブル2枚以上]スキルカードを引く, PLv51, レッスン中1回
        CardRegistry.register(b("card_anomaly_turnaround", "形成逆転", CardType.ONCE_PER_LESSON)
                .description("強気に変更、パラメータ+9、トラブルカード2枚以上の場合、スキルカードを引く")
                .hpCost(4).baseScore(9).requiredPLevel(51)
                .effect((p, d) -> {
                    setBold(d, 1);
                    dmg(p, d, 9);
                    if (troubleAtLeast(d, 2)) d.getBuffState().addPendingDraw(1);
                }).build());

        // ノンストップ: コスト3, 全力値+2 全力の場合、パラメータ+20, PLv52, レッスン中1回
        // 近似: 「以降1回まで、ターン開始時」は即時判定に単純化。「手札を保留に移動」は保留ゾーン未実装のため省略
        CardRegistry.register(b("card_anomaly_nonstop", "ノンストップ", CardType.ONCE_PER_LESSON)
                .description("全力値+2、全力の場合、パラメータ+20").hpCost(3).requiredPLevel(52)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(2);
                    if (d.getBuffState().getZenryoku() > 0) dmg(p, d, 20);
                }).build());

        // ハッスル: コスト4, 強気に変更 全力値+2 アクティブスキルカードのパラメータ値増加+4(永続), PLv71, レッスン中1回
        CardRegistry.register(b("card_anomaly_hustle", "ハッスル", CardType.ONCE_PER_LESSON)
                .description("強気に変更、全力値+2、以降アクティブスキルカードのパラメータ値増加+4（重複可）")
                .hpCost(4).requiredPLevel(71)
                .effect((p, d) -> {
                    setBold(d, 1);
                    d.getBuffState().addZenryoku(2);
                    d.getBuffState().addActiveParamBonus(4);
                }).build());

        // ============ R（銀） メンタル ============

        // ハッピー♪: コスト3, 温存に変更 元気+7
        CardRegistry.register(b("card_anomaly_happy_note", "ハッピー♪", CardType.NORMAL)
                .description("温存に変更、元気+7").hpCost(3).category(CardCategory.MENTAL)
                .effect((p, d) -> { setConserve(d, 1); GenkiHelper.addGenki(p, 7f); }).build());

        // 嬉しい誤算: コスト3, 元気+5 全力値+2 全力値が1以上の場合、温存に変更, PLv4, レッスン中1回
        CardRegistry.register(b("card_anomaly_happy_accident", "嬉しい誤算", CardType.ONCE_PER_LESSON)
                .description("元気+5、全力値+2、全力値が1以上の場合、温存に変更")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(4)
                .effect((p, d) -> {
                    GenkiHelper.addGenki(p, 5f);
                    d.getBuffState().addZenryoku(2);
                    if (d.getBuffState().getZenryoku() >= 1) setConserve(d, 1);
                }).build());

        // 涙の思い出: コスト2, 温存に変更 元気+2 強気の場合、全力値+3, PLv19, レッスン中1回
        CardRegistry.register(b("card_anomaly_tearful_memory", "涙の思い出", CardType.ONCE_PER_LESSON)
                .description("温存に変更、元気+2、強気の場合、全力値+3")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(19)
                .effect((p, d) -> {
                    setConserve(d, 1);
                    GenkiHelper.addGenki(p, 2f);
                    if (d.getBuffState().isBold()) d.getBuffState().addZenryoku(3);
                }).build());

        // セッティング: コスト3, 条件:全力値5以上, 全力値+5 次のターン、手札のパラメータ値増加+8, PLv47, 重複不可/レッスン中1回
        // 近似: 「次のターン、手札のパラメータ値増加+8」はアクティブ永続ボーナス+8として即時適用する
        CardRegistry.register(b("card_anomaly_setting", "セッティング", CardType.ONCE_PER_LESSON)
                .description("全力値+5、以降アクティブスキルカードのパラメータ値増加+8")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(47)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 5, "全力値5以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(5);
                    d.getBuffState().addActiveParamBonus(8);
                }).build());

        // 巻き返し: コスト"全力値1", 温存に変更 熱意増加+3% すべてのスキルカードのコスト値減少-1(永続), PLv66, レッスン中1回
        CardRegistry.register(b("card_anomaly_comeback", "巻き返し", CardType.ONCE_PER_LESSON)
                .description("全力値1消費、温存に変更、熱意増加+3%、以降消費体力減少")
                .category(CardCategory.MENTAL).requiredPLevel(66)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 1, "全力値1以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(1);
                    setConserve(d, 1);
                    d.getBuffState().addEnthusiasm(3, 9999);
                    d.getBuffState().addCostReductionTurns(9999);
                }).build());

        // ============ SR（金） アクティブ ============

        // せーのっ!: コスト5, 強気に変更 パラメータ+12
        CardRegistry.register(b("card_anomaly_ready_set", "せーのっ!", CardType.NORMAL)
                .description("強気に変更、パラメータ+12").hpCost(5).baseScore(12)
                .effect((p, d) -> { setBold(d, 1); dmg(p, d, 12); }).build());

        // アッチェレランド: コスト5, パラメータ+10 全力値+3
        CardRegistry.register(b("card_anomaly_accelerando", "アッチェレランド", CardType.NORMAL)
                .description("パラメータ+10、全力値+3").hpCost(5).baseScore(10)
                .effect((p, d) -> { dmg(p, d, 10); d.getBuffState().addZenryoku(3); }).build());

        // はじけるパッション: コスト2, 条件:いずれかの指針, パラメータ+8（2回）, PLv6
        CardRegistry.register(b("card_anomaly_bursting_passion", "はじけるパッション", CardType.NORMAL)
                .description("パラメータ+8（2回）").hpCost(2).baseScore(16).requiredPLevel(6)
                .usableWhen((pl, d) -> d.getBuffState().hasAnyPolicy(), "強気または温存が必要")
                .effect((p, d) -> dmgTimes(p, d, 8, 2)).build());

        // 汗と成長: コスト1, 条件:全力, パラメータ+37, PLv7, レッスン中1回
        CardRegistry.register(b("card_anomaly_sweat_and_growth", "汗と成長", CardType.ONCE_PER_LESSON)
                .description("パラメータ+37").hpCost(1).baseScore(37).requiredPLevel(7)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() > 0, "全力が必要")
                .effect((p, d) -> dmg(p, d, 37)).build());

        // 第一印象: コスト5, 強気に変更 以降、強気効果のスキルカード使用時、すべてのスキルカードのパラメータ値増加+2(永続), PLv18, 重複不可/レッスン中1回
        // 近似: 「強気効果のスキルカード使用時」限定の判定は煩雑なため、アクティブスキルカード全般への永続ボーナスとして実装する。
        CardRegistry.register(b("card_anomaly_first_impression", "第一印象", CardType.ONCE_PER_LESSON)
                .description("強気に変更、以降アクティブスキルカードのパラメータ値増加+2")
                .hpCost(5).category(CardCategory.MENTAL).requiredPLevel(18)
                .effect((p, d) -> {
                    setBold(d, 1);
                    d.getBuffState().addActiveParamBonus(2);
                }).build());

        // オープニングアクト: コスト5, 強気に変更 パラメータ+10 全力値+2, PLv29, レッスン中1回
        CardRegistry.register(b("card_anomaly_opening_act", "オープニングアクト", CardType.ONCE_PER_LESSON)
                .description("強気に変更、パラメータ+10、全力値+2").hpCost(5).baseScore(10).requiredPLevel(29)
                .effect((p, d) -> { setBold(d, 1); dmg(p, d, 10); d.getBuffState().addZenryoku(2); }).build());

        // 始まりの笑顔: コスト"全力値2", 強気に変更 パラメータ+6 消費体力減少2ターン 次のターン、スキルカードを引く, PLv34, 重複不可
        CardRegistry.register(b("card_anomaly_starting_smile", "始まりの笑顔", CardType.NORMAL)
                .description("全力値2消費、強気に変更、パラメータ+6、消費体力減少2ターン、スキルカードを引く")
                .baseScore(6).requiredPLevel(34)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 2, "全力値2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(2);
                    setBold(d, 1);
                    dmg(p, d, 6);
                    d.getBuffState().addCostReductionTurns(2);
                    d.getBuffState().addPendingDraw(1);
                }).build());

        // トレンドリーダー: コスト"全力値2", 非全力ならスキルカード使用数追加+1／全力ならパラメータ+3(累積全力値50%分・2回), PLv36, 重複不可
        // 近似: 「自身を保留に移動」「成長」は省略
        CardRegistry.register(b("card_anomaly_trend_leader", "トレンドリーダー", CardType.NORMAL)
                .description("全力値2消費、非全力状態ならスキルカード使用数追加+1、全力状態ならパラメータ+3（累積全力値50%分・2回）")
                .requiredPLevel(36)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 2, "全力値2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(2);
                    if (d.getBuffState().getZenryoku() <= 0) {
                        d.getBuffState().addBonusAction(1);
                    } else {
                        dmgTimes(p, d, 3 + pctOfZenryokuAccumulated(d, 50), 2);
                    }
                }).build());

        // 理想のテンポ: コスト5, パラメータ+6（2回） 非温存状態ならスキルカード使用数追加+1, PLv52, レッスン中1回
        // 近似: 「成長：指針を変更するたび」は省略
        CardRegistry.register(b("card_anomaly_ideal_tempo", "理想のテンポ", CardType.ONCE_PER_LESSON)
                .description("パラメータ+6（2回）、非温存状態の場合、スキルカード使用数追加+1")
                .hpCost(5).baseScore(12).requiredPLevel(52)
                .effect((p, d) -> {
                    dmgTimes(p, d, 6, 2);
                    if (!d.getBuffState().isConserving()) d.getBuffState().addBonusAction(1);
                }).build());

        // トレーニングの成果: コスト5, 全力値+3 パラメータ+28（累積全力値100%分）, PLv58, レッスン中1回
        // 近似: 「以降1回まで、全力解除後に温存2段階目」は省略
        CardRegistry.register(b("card_anomaly_training_result", "トレーニングの成果", CardType.ONCE_PER_LESSON)
                .description("全力値+3、パラメータ+28（累積全力値100%分、パラメータ上昇量増加）")
                .hpCost(5).requiredPLevel(58)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(3);
                    dmg(p, d, 28 + pctOfZenryokuAccumulated(d, 100));
                }).build());

        // オーバードライブ: コスト5, 強気に変更 パラメータ+4 全力値+2 スキルカード使用数追加+1 眠気生成 温存に変更, PLv58, 重複不可
        // 近似: 「次のターン、温存に変更」は即時適用に単純化
        CardRegistry.register(b("card_anomaly_overdrive", "オーバードライブ", CardType.NORMAL)
                .description("強気に変更、パラメータ+4、全力値+2、スキルカード使用数追加+1、眠気を山札に生成、温存に変更")
                .hpCost(5).requiredPLevel(58)
                .effect((p, d) -> {
                    setBold(d, 1);
                    dmg(p, d, 4);
                    d.getBuffState().addZenryoku(2);
                    d.getBuffState().addBonusAction(1);
                    spawnDrowsiness(d);
                    setConserve(d, 1);
                }).build());

        // アンダンテ: コスト"全力値2", 強気に変更 パラメータ+10 熱意増加+10%, PLv73, レッスン中1回
        CardRegistry.register(b("card_anomaly_andante", "アンダンテ", CardType.ONCE_PER_LESSON)
                .description("全力値2消費、強気に変更、パラメータ+10、熱意増加+10%")
                .requiredPLevel(73)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 2, "全力値2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(2);
                    setBold(d, 1);
                    dmg(p, d, 10);
                    d.getBuffState().addEnthusiasm(10, 9999);
                }).build());

        // ============ SR（金） メンタル ============

        // 潜在能力: コスト6, 元気+10 全力値+3, レッスン中1回（「手札を保留に移動」は省略）
        CardRegistry.register(b("card_anomaly_potential", "潜在能力", CardType.ONCE_PER_LESSON)
                .description("元気+10、全力値+3").hpCost(6).category(CardCategory.MENTAL)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 10f); d.getBuffState().addZenryoku(3); }).build());

        // カウントダウン: コスト2, 温存に変更 元気+8, レッスン中1回
        CardRegistry.register(b("card_anomaly_countdown", "カウントダウン", CardType.ONCE_PER_LESSON)
                .description("温存に変更、元気+8").hpCost(2).category(CardCategory.MENTAL)
                .effect((p, d) -> { setConserve(d, 1); GenkiHelper.addGenki(p, 8f); }).build());

        // モチベ: コスト4, （effect_baseが空のためeffect_upgradedを採用）全力値+2 以降、全力効果のスキルカード使用時、全力値+1, PLv17, 重複不可/レッスン中1回
        // 近似: 「全力効果のスキルカード使用時」は判定が煩雑なため、全力状態のカード使用時全般（全力値>0で他カードを使った時）に単純化
        CardRegistry.register(b("card_anomaly_motivation_boost", "モチベ", CardType.ONCE_PER_LESSON)
                .description("全力値+2").hpCost(4).category(CardCategory.MENTAL).requiredPLevel(17)
                .effect((p, d) -> d.getBuffState().addZenryoku(2)).build());

        // プライド: コスト4, レッスン開始時手札に入る 温存に変更 元気+5 全力値+2, PLv20, レッスン中1回
        CardRegistry.register(b("card_anomaly_pride", "プライド", CardType.ONCE_PER_LESSON)
                .description("レッスン開始時手札に入る、温存に変更、元気+5、全力値+2")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(20).guaranteedFirstDraw(true)
                .effect((p, d) -> {
                    setConserve(d, 1);
                    GenkiHelper.addGenki(p, 5f);
                    d.getBuffState().addZenryoku(2);
                }).build());

        // 盛り上げ上手: コスト4, アクティブスキルカードのパラメータ値増加+13(永続) 次ターン、スキルカードを引く, PLv22, 重複不可/レッスン中1回
        // 近似: 「コスト値増加+1」・「次のターン」は省略/即時ドローに単純化
        CardRegistry.register(b("card_anomaly_hype_master", "盛り上げ上手", CardType.ONCE_PER_LESSON)
                .description("以降アクティブスキルカードのパラメータ値増加+13、スキルカードを引く")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(22)
                .effect((p, d) -> {
                    d.getBuffState().addActiveParamBonus(13);
                    d.getBuffState().addPendingDraw(1);
                }).build());

        // インフルエンサー: コスト6, メンタルスキルカードの元気値増加+5(永続) スキルカード使用数追加+1 スキルカードを引く, PLv24, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_influencer", "インフルエンサー", CardType.ONCE_PER_LESSON)
                .description("以降メンタルスキルカード使用時、元気+5、スキルカード使用数追加+1、スキルカードを引く")
                .hpCost(6).category(CardCategory.MENTAL).requiredPLevel(24)
                .effect((p, d) -> {
                    d.getBuffState().addOnMentalUseGenki(5);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(1);
                }).build());

        // 忍耐力: コスト2, 温存に変更 元気+5 全力値+2, PLv28, レッスン中1回
        CardRegistry.register(b("card_anomaly_patience", "忍耐力", CardType.ONCE_PER_LESSON)
                .description("温存に変更、元気+5、全力値+2")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(28)
                .effect((p, d) -> {
                    setConserve(d, 1);
                    GenkiHelper.addGenki(p, 5f);
                    d.getBuffState().addZenryoku(2);
                }).build());

        // 切磋琢磨: コスト1, 全力値+2 スキルカード使用数追加+1, PLv32, 重複不可
        // 近似: 「山札/捨札からカード選択し保留移動」「指針固定1ターン」は省略
        CardRegistry.register(b("card_anomaly_mutual_improvement", "切磋琢磨", CardType.NORMAL)
                .description("全力値+2、スキルカード使用数追加+1")
                .hpCost(1).category(CardCategory.MENTAL).requiredPLevel(32)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(2);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // フルスロットル: コスト2, 温存に変更 熱意増加+100%(3ターン) スキルカード使用数追加+1 3枚引く 眠気生成, PLv53, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_full_throttle", "フルスロットル", CardType.ONCE_PER_LESSON)
                .description("温存に変更、熱意増加+100%（3ターン）、スキルカード使用数追加+1、スキルカードを3枚引く、眠気を山札に生成")
                .hpCost(2).category(CardCategory.MENTAL).requiredPLevel(53)
                .effect((p, d) -> {
                    setConserve(d, 1);
                    d.getBuffState().addEnthusiasm(100, 3);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(3);
                    spawnDrowsiness(d);
                }).build());

        // リスキーチャンス: コスト4, 全力値+3 スキルカード使用数追加+1 2枚引く 眠気生成, PLv54, 重複不可/レッスン中1回
        // 近似: 「全力値増加量増加+50%(3ターン)」は省略
        CardRegistry.register(b("card_anomaly_risky_chance", "リスキーチャンス", CardType.ONCE_PER_LESSON)
                .description("全力値+3、スキルカード使用数追加+1、スキルカードを2枚引く、眠気を山札に生成")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(54)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(3);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(2);
                    spawnDrowsiness(d);
                }).build());

        // タフネス: コスト5, 条件:温存になった回数2回以上, 温存に変更 スキルカード使用数追加+1 アクティブ永続+14, PLv63, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_toughness", "タフネス", CardType.ONCE_PER_LESSON)
                .description("温存に変更、スキルカード使用数追加+1、以降アクティブスキルカードのパラメータ値増加+14")
                .hpCost(5).category(CardCategory.MENTAL).requiredPLevel(63)
                .usableWhen((pl, d) -> d.getBuffState().getConserveActivations() >= 2, "温存になった回数が2回以上必要")
                .effect((p, d) -> {
                    setConserve(d, 1);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addActiveParamBonus(14);
                }).build());

        // 達成感: コスト6, 温存に変更 スキルカード使用数追加+1, PLv68, 重複不可/レッスン中1回
        // 近似: 「以降3回まで、全力解除後に温存へ変更」は省略
        CardRegistry.register(b("card_anomaly_sense_of_achievement", "達成感", CardType.ONCE_PER_LESSON)
                .description("温存に変更、スキルカード使用数追加+1")
                .hpCost(6).category(CardCategory.MENTAL).requiredPLevel(68)
                .effect((p, d) -> {
                    setConserve(d, 1);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // ============ SSR（虹） アクティブ ============

        // 翔び立て: コスト6, 全力値+3 パラメータ+10（累積全力値100%分）, PLv11, 重複不可/レッスン中1回
        // 近似: 「成長：全力になった時+20（1回まで）」は省略
        CardRegistry.register(b("card_anomaly_take_flight", "翔び立て", CardType.ONCE_PER_LESSON)
                .description("全力値+3、パラメータ+10（累積全力値100%分、パラメータ上昇量増加）")
                .hpCost(6).requiredPLevel(11)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(3);
                    dmg(p, d, 10 + pctOfZenryokuAccumulated(d, 100));
                }).build());

        // 総合芸術: コスト7, 条件:いずれかの指針, 強気に変更 パラメータ+7 成長(自身の再使用毎に+15、4回まで), PLv12, 重複不可
        CardRegistry.register(b("card_anomaly_total_art", "総合芸術", CardType.NORMAL)
                .description("強気に変更、パラメータ+7（このカードを指針状態で使うたび、成長して威力上昇・4回まで）")
                .requiredPLevel(12)
                .usableWhen((pl, d) -> d.getBuffState().hasAnyPolicy(), "強気または温存が必要")
                .effect((p, d) -> {
                    setBold(d, 1);
                    long growth = d.getBuffState().getGrowthStacks("card_anomaly_total_art");
                    dmg(p, d, 7 + growth * 15);
                    d.getBuffState().addGrowthStacks("card_anomaly_total_art", 1, 4);
                }).build());

        // 心・技・体: コスト8, 強気2段階目に変更 パラメータ+7 全力値+3 消費体力減少2ターン, PLv30, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_mind_body_spirit", "心・技・体", CardType.ONCE_PER_LESSON)
                .description("強気2段階目に変更、パラメータ+7、全力値+3、消費体力減少2ターン")
                .hpCost(8).requiredPLevel(30)
                .effect((p, d) -> {
                    setBold(d, 2);
                    dmg(p, d, 7);
                    d.getBuffState().addZenryoku(3);
                    d.getBuffState().addCostReductionTurns(2);
                }).build());

        // 輝け!: コスト7, 全力値+3 温存に変更 全力の場合、パラメータ+20（2回）, PLv41, 重複不可/レッスン中1回
        // 近似: 「次のターン、温存に変更」は即時適用に単純化
        CardRegistry.register(b("card_anomaly_shine", "輝け!", CardType.ONCE_PER_LESSON)
                .description("全力値+3、温存に変更、全力の場合、パラメータ+20（2回）")
                .hpCost(7).requiredPLevel(41)
                .effect((p, d) -> {
                    d.getBuffState().addZenryoku(3);
                    setConserve(d, 1);
                    if (d.getBuffState().getZenryoku() > 0) dmgTimes(p, d, 20, 2);
                }).build());

        // クライマックス: コスト10, 強気に変更 パラメータ+14（2回） 温存に変更, PLv43, 重複不可/レッスン中1回
        // 近似: 「次のターン、温存に変更」は即時適用に単純化
        CardRegistry.register(b("card_anomaly_climax", "クライマックス", CardType.ONCE_PER_LESSON)
                .description("強気に変更、パラメータ+14（2回）、温存に変更")
                .hpCost(10).requiredPLevel(43)
                .effect((p, d) -> {
                    setBold(d, 1);
                    dmgTimes(p, d, 14, 2);
                    setConserve(d, 1);
                }).build());

        // 全身全霊: コスト8, 強気に変更 次に使用するカードの消費体力を0にする(2回) スキルカード使用数追加+1 3枚引く, PLv50, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_all_in", "全身全霊", CardType.ONCE_PER_LESSON)
                .description("強気に変更、次に使用するスキルカードの消費体力を0にする（2回）、スキルカード使用数追加+1、スキルカードを3枚引く")
                .hpCost(8).requiredPLevel(50)
                .effect((p, d) -> {
                    setBold(d, 1);
                    d.getBuffState().addFreeCostUses(2);
                    d.getBuffState().addBonusAction(1);
                    d.getBuffState().addPendingDraw(3);
                }).build());

        // エンターテイナー: コスト5, 強気に変更 パラメータ+10, PLv77, 重複不可（「成長：指針を3回変更するたび」は省略）
        CardRegistry.register(b("card_anomaly_entertainer", "エンターテイナー", CardType.NORMAL)
                .description("強気に変更、パラメータ+10").hpCost(5).requiredPLevel(77)
                .effect((p, d) -> { setBold(d, 1); dmg(p, d, 10); }).build());

        // 羽ばたけ!: コスト"全力値4", 条件:累計全力値5以上, 全力値+4 全力の場合、パラメータ+5（4回）, PLv80, 重複不可
        // 近似: 「自身を保留に移動」「次のターン」「成長」は省略し、即時効果として実装
        CardRegistry.register(b("card_anomaly_soar", "羽ばたけ!", CardType.NORMAL)
                .description("全力値4消費、全力値+4、全力の場合、パラメータ+5（4回）")
                .requiredPLevel(80)
                .usableWhen((pl, d) -> d.getBuffState().getZenryokuAccumulated() >= 5 && d.getBuffState().getZenryoku() >= 4,
                        "累計全力値5以上・全力値4以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(4);
                    d.getBuffState().addZenryoku(4);
                    if (d.getBuffState().getZenryoku() > 0) dmgTimes(p, d, 5, 4);
                }).build());

        // ============ SSR（虹） メンタル ============

        // アイドルになります: コスト"全力値3", スキルカード使用数追加+1, PLv80(データのPLv8025は誤記とみなす), 重複不可/レッスン中1回
        // 近似: 「手札のパラメータ上昇回数増加+1」は省略
        CardRegistry.register(b("card_anomaly_becoming_idol", "アイドルになります", CardType.ONCE_PER_LESSON)
                .description("全力値3消費、スキルカード使用数追加+1")
                .category(CardCategory.MENTAL).requiredPLevel(80)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 3, "全力値3以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(3);
                    d.getBuffState().addBonusAction(1);
                }).build());

        // 一心不乱: コスト3, 条件:強気, 温存に変更 以降アクティブスキルカードのパラメータ値増加+37(永続), PLv38, 重複不可/レッスン中1回
        CardRegistry.register(b("card_anomaly_single_minded", "一心不乱", CardType.ONCE_PER_LESSON)
                .description("温存に変更、以降アクティブスキルカードのパラメータ値増加+37")
                .hpCost(3).category(CardCategory.MENTAL).requiredPLevel(38)
                .usableWhen((pl, d) -> d.getBuffState().isBold(), "強気が必要")
                .effect((p, d) -> {
                    setConserve(d, 1);
                    d.getBuffState().addActiveParamBonus(37);
                }).build());

        // 頂点へ: コスト9, いずれかの指針の場合、アクティブ永続+4, PLv45, 重複不可/レッスン中1回
        // 近似: 「以降、ターン開始時」の継続判定は、発動時点で指針が有効なら即時付与する形に単純化
        CardRegistry.register(b("card_anomaly_to_the_top", "頂点へ", CardType.ONCE_PER_LESSON)
                .description("強気または温存の場合、以降アクティブスキルカードのパラメータ値増加+4")
                .hpCost(9).category(CardCategory.MENTAL).requiredPLevel(45)
                .effect((p, d) -> {
                    if (d.getBuffState().hasAnyPolicy()) d.getBuffState().addActiveParamBonus(4);
                }).build());

        // 覚悟: コスト4, 元気+8 全力値+5, PLv54, 重複不可/レッスン中1回（「山札/捨札から選択し保留移動」は省略）
        CardRegistry.register(b("card_anomaly_resolve", "覚悟", CardType.ONCE_PER_LESSON)
                .description("元気+8、全力値+5")
                .hpCost(4).category(CardCategory.MENTAL).requiredPLevel(54)
                .effect((p, d) -> { GenkiHelper.addGenki(p, 8f); d.getBuffState().addZenryoku(5); }).build());

        // 本領発揮: コスト"全力値2", 次のターン、全力値+5, 重複不可
        // 近似: 「山札/捨札から選択し保留移動」「保留カードのパラメータ上昇回数増加・コスト値増加」は保留ゾーン未実装のため省略。
        // 「次のターン、全力値+5」は即時適用に単純化。PLevel未記載のため制限なしとする。
        CardRegistry.register(b("card_anomaly_true_potential", "本領発揮", CardType.NORMAL)
                .description("全力値2消費、全力値+5")
                .category(CardCategory.MENTAL)
                .usableWhen((pl, d) -> d.getBuffState().getZenryoku() >= 2, "全力値2以上が必要")
                .effect((p, d) -> {
                    d.getBuffState().spendZenryoku(2);
                    d.getBuffState().addZenryoku(5);
                }).build());
    }
}
