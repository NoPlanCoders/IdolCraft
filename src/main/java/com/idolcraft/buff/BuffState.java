package com.idolcraft.buff;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * プレイヤー1人分のバフ状態。
 * 拡張性のため、汎用パッシブ効果（例：「演出計画」のカード使用時元気+2）は
 * 文字列キーの {@link #passiveFlags} セットに保持し、新規パッシブを追加しても
 * このクラス自体を変更しなくて済むようにしている。
 *
 * 集中スタック・好調/絶好調のTick・各種カウンタはすべて long で保持し、
 * 高倍率・大量スタック時の int オーバーフローを避ける。
 */
public class BuffState {

    /** 集中スタック数（永続。1ターン=5秒換算の時間経過では減らない） */
    private long focusStacks = 0;

    /** 好調の残りTick数（20Tick=1秒。リアルタイムで毎Tick減少） */
    private long goodConditionTicks = 0;

    /** 絶好調の残りTick数（同上） */
    private long greatConditionTicks = 0;

    /**
     * 好調が「今回のバフ期間中」に延べ何ターン分付与されたかを保持するカウンタ。
     * 学マス本家の絶好調計算式（好調のターン数に応じて倍率が変わる）を再現するために使用する。
     * 好調が完全に0になった時点で0にリセットする。
     */
    private long goodConditionTurnsAccumulated = 0;

    /** 汎用パッシブ効果フラグ（例: "encore_genki_on_use" = 演出計画のカード使用時元気+2） */
    private final Set<String> passiveFlags = new HashSet<>();

    /** 拡張用の汎用整数値ストレージ（long）。将来の新バフ・新プランで自由に使える */
    private final Map<String, Long> customCounters = new HashMap<>();

    public long getFocusStacks() {
        return focusStacks;
    }

    public void addFocus(long amount) {
        this.focusStacks = Math.max(0L, this.focusStacks + amount);
    }

    public long getGoodConditionTicks() {
        return goodConditionTicks;
    }

    public boolean isGoodConditionActive() {
        return goodConditionTicks > 0;
    }

    /**
     * 好調を付与する。学マス仕様同様、重複付与は「持続時間の延長」として扱う（上書きではなく加算）。
     * 1ターン=5秒=100Tick換算で呼び出し側から渡されたTick数をそのまま加算する。
     */
    public void addGoodCondition(long ticks, long turns) {
        this.goodConditionTicks = Math.max(0L, this.goodConditionTicks + ticks);
        this.goodConditionTurnsAccumulated = Math.max(0L, this.goodConditionTurnsAccumulated + turns);
    }

    public long getGreatConditionTicks() {
        return greatConditionTicks;
    }

    public boolean isGreatConditionActive() {
        return greatConditionTicks > 0;
    }

    public void addGreatCondition(long ticks) {
        this.greatConditionTicks = Math.max(0L, this.greatConditionTicks + ticks);
    }

    /** 集中・好調・絶好調だけをまとめて消す。進捗やパッシブフラグは残す。 */
    public void clearStatusBuffs() {
        focusStacks = 0;
        goodConditionTicks = 0;
        greatConditionTicks = 0;
        goodConditionTurnsAccumulated = 0;
    }

    public long getGoodConditionTurnsAccumulated() {
        return goodConditionTurnsAccumulated;
    }

    public boolean hasPassiveFlag(String flag) {
        return passiveFlags.contains(flag);
    }

    public void setPassiveFlag(String flag, boolean value) {
        if (value) passiveFlags.add(flag);
        else passiveFlags.remove(flag);
    }

    public long getCustomCounter(String key) {
        return customCounters.getOrDefault(key, 0L);
    }

    public void setCustomCounter(String key, long value) {
        customCounters.put(key, value);
    }

    /** 「スキルカード使用数追加」用カウンタキー。ターンを終えず続けてもう1枚使えるボーナスの残数。 */
    private static final String BONUS_ACTION_KEY = "bonus_actions";

    public long getBonusActions() {
        return getCustomCounter(BONUS_ACTION_KEY);
    }

    /** 「シュプレヒコール」等：このカード使用後、ターンを終えずもう1枚使用できるボーナスを加算する */
    public void addBonusAction(long amount) {
        setCustomCounter(BONUS_ACTION_KEY, Math.max(0L, getBonusActions() + amount));
    }

    /** ボーナスを1消費する（残数が無ければ何もしない） */
    public void consumeBonusAction() {
        long current = getBonusActions();
        if (current > 0) {
            setCustomCounter(BONUS_ACTION_KEY, current - 1);
        }
    }

    /** ターンをまたいでボーナスを持ち越さないようにするためのクリア処理 */
    public void clearBonusActions() {
        setCustomCounter(BONUS_ACTION_KEY, 0);
    }

    // ── 追加ドロー（効果解決後にまとめて山札から引く予約） ──
    private static final String PENDING_DRAW_KEY = "pending_draw";
    public long getPendingDraw() { return getCustomCounter(PENDING_DRAW_KEY); }
    public void addPendingDraw(long n) { setCustomCounter(PENDING_DRAW_KEY, Math.max(0L, getPendingDraw() + n)); }
    public void clearPendingDraw() { setCustomCounter(PENDING_DRAW_KEY, 0); }

    // ── 消費体力減少（残ターン数。>0 の間、カードの消費体力を軽減する） ──
    private static final String COST_REDUCTION_KEY = "cost_reduction_turns";
    public long getCostReductionTurns() { return getCustomCounter(COST_REDUCTION_KEY); }
    public void addCostReductionTurns(long t) { setCustomCounter(COST_REDUCTION_KEY, Math.max(0L, getCostReductionTurns() + t)); }
    public void tickCostReductionTurn() {
        long c = getCostReductionTurns();
        if (c > 0) setCustomCounter(COST_REDUCTION_KEY, c - 1);
    }

    // ── 次に使うカードの効果を2回発動する残数（「国民的アイドル」等） ──
    private static final String DOUBLE_NEXT_KEY = "double_next_cards";
    public long getDoubleNextCards() { return getCustomCounter(DOUBLE_NEXT_KEY); }
    public void addDoubleNextCards(long n) { setCustomCounter(DOUBLE_NEXT_KEY, Math.max(0L, getDoubleNextCards() + n)); }
    public void consumeDoubleNextCard() {
        long c = getDoubleNextCards();
        if (c > 0) setCustomCounter(DOUBLE_NEXT_KEY, c - 1);
    }

    // ── 継続パラメータ（ターン終了毎にこの値ぶんターゲットへダメージ。「至高のエンタメ」等） ──
    private static final String PARAM_PER_TURN_KEY = "param_per_turn";
    public long getParamPerTurn() { return getCustomCounter(PARAM_PER_TURN_KEY); }
    public void addParamPerTurn(long n) { setCustomCounter(PARAM_PER_TURN_KEY, Math.max(0L, getParamPerTurn() + n)); }

    // ================================================================
    // ロジックプラン：やる気・好印象（集中と同じく永続スタック型）
    // ================================================================
    private static final String MOTIVATION_KEY = "motivation";
    public long getMotivation() { return getCustomCounter(MOTIVATION_KEY); }
    public void addMotivation(long amount) { setCustomCounter(MOTIVATION_KEY, Math.max(0L, getMotivation() + amount)); }

    private static final String IMPRESSION_KEY = "good_impression";
    public long getGoodImpression() { return getCustomCounter(IMPRESSION_KEY); }
    public void addGoodImpression(long amount) { setCustomCounter(IMPRESSION_KEY, Math.max(0L, getGoodImpression() + amount)); }

    // 好印象強化（一定ターンの間、好印象増加量を割合で上乗せする。ターン経過はDeckService側でtickする）
    private static final String IMPRESSION_BOOST_PCT_KEY = "impression_boost_pct";
    private static final String IMPRESSION_BOOST_TURNS_KEY = "impression_boost_turns";
    public long getImpressionBoostPercent() { return getCustomCounter(IMPRESSION_BOOST_PCT_KEY); }
    public long getImpressionBoostTurns() { return getCustomCounter(IMPRESSION_BOOST_TURNS_KEY); }
    public void addImpressionBoost(long percent, long turns) {
        setCustomCounter(IMPRESSION_BOOST_PCT_KEY, Math.max(0L, getImpressionBoostPercent() + percent));
        setCustomCounter(IMPRESSION_BOOST_TURNS_KEY, Math.max(0L, getImpressionBoostTurns() + turns));
    }
    public void tickImpressionBoostTurn() {
        long t = getImpressionBoostTurns();
        if (t > 0) {
            setCustomCounter(IMPRESSION_BOOST_TURNS_KEY, t - 1);
            if (t - 1 == 0) setCustomCounter(IMPRESSION_BOOST_PCT_KEY, 0);
        }
    }

    // ================================================================
    // アノマリープラン：全力値・強気・温存・熱意・成長
    // ================================================================

    /** 全力値：スタック型。カードのコスト（「全力値N」）として直接消費されることもある */
    private static final String ZENRYOKU_KEY = "zenryoku";
    private static final String ZENRYOKU_ACCUMULATED_KEY = "zenryoku_accumulated";
    public long getZenryoku() { return getCustomCounter(ZENRYOKU_KEY); }
    /** このレッスン中に累計で獲得した全力値（消費しても減らない。「羽ばたけ!」等の条件判定用） */
    public long getZenryokuAccumulated() { return getCustomCounter(ZENRYOKU_ACCUMULATED_KEY); }
    public void addZenryoku(long amount) {
        setCustomCounter(ZENRYOKU_KEY, Math.max(0L, getZenryoku() + amount));
        if (amount > 0) setCustomCounter(ZENRYOKU_ACCUMULATED_KEY, getZenryokuAccumulated() + amount);
    }
    /** 全力値をコストとして消費する。足りない場合は0まで消費して打ち切る */
    public void spendZenryoku(long amount) {
        setCustomCounter(ZENRYOKU_KEY, Math.max(0L, getZenryoku() - amount));
    }

    /** 強気の段階（0=無し, 1=強気, 2=強気2段階目）。「変更」効果なので加算ではなく上書き（下位互換のため最大値を保持） */
    private static final String BOLD_STAGE_KEY = "bold_stage";
    public long getBoldStage() { return getCustomCounter(BOLD_STAGE_KEY); }
    public boolean isBold() { return getBoldStage() >= 1; }
    public boolean isBoldStage2() { return getBoldStage() >= 2; }
    public void setBoldStageAtLeast(long stage) { setCustomCounter(BOLD_STAGE_KEY, Math.max(getBoldStage(), stage)); }

    /** 温存の段階（0=無し, 1=温存, 2=温存2段階目） */
    private static final String CONSERVE_STAGE_KEY = "conserve_stage";
    public long getConserveStage() { return getCustomCounter(CONSERVE_STAGE_KEY); }
    public boolean isConserving() { return getConserveStage() >= 1; }
    public boolean isConservingStage2() { return getConserveStage() >= 2; }
    public void setConserveStageAtLeast(long stage) { setCustomCounter(CONSERVE_STAGE_KEY, Math.max(getConserveStage(), stage)); }

    /** 「指針」：強気・温存のいずれかが有効かどうか（本Modでは強気/温存をまとめて指針として扱う近似） */
    public boolean hasAnyPolicy() { return isBold() || isConserving(); }

    /** 熱意増加：一定ターンの間、アクティブスキルカードのパラメータ上昇量を割合で上乗せする */
    private static final String ENTHUSIASM_PCT_KEY = "enthusiasm_pct";
    private static final String ENTHUSIASM_TURNS_KEY = "enthusiasm_turns";
    public long getEnthusiasmPercent() { return getCustomCounter(ENTHUSIASM_PCT_KEY); }
    public long getEnthusiasmTurns() { return getCustomCounter(ENTHUSIASM_TURNS_KEY); }
    public void addEnthusiasm(long percent, long turns) {
        setCustomCounter(ENTHUSIASM_PCT_KEY, Math.max(0L, getEnthusiasmPercent() + percent));
        setCustomCounter(ENTHUSIASM_TURNS_KEY, Math.max(0L, getEnthusiasmTurns() + turns));
    }
    public void tickEnthusiasmTurn() {
        long t = getEnthusiasmTurns();
        if (t > 0) {
            setCustomCounter(ENTHUSIASM_TURNS_KEY, t - 1);
            if (t - 1 == 0) setCustomCounter(ENTHUSIASM_PCT_KEY, 0);
        }
    }

    /** 汎用の「成長」カウンタ：カードごとに growth_<カードのpath> キーで積み上げる自己強化回数 */
    public long getGrowthStacks(String cardPath) { return getCustomCounter("growth_" + cardPath); }
    public void addGrowthStacks(String cardPath, long amount, long max) {
        long cur = getGrowthStacks(cardPath);
        if (cur < max) setCustomCounter("growth_" + cardPath, Math.min(max, cur + amount));
    }

    // ── カテゴリ条件付きの汎用パッシブ（「メンタルスキルカード使用時、やる気+1」等） ──
    public long getOnMentalUseMotivation() { return getCustomCounter("on_mental_use_motivation"); }
    public void addOnMentalUseMotivation(long n) { setCustomCounter("on_mental_use_motivation", getOnMentalUseMotivation() + n); }
    public long getOnMentalUseImpression() { return getCustomCounter("on_mental_use_impression"); }
    public void addOnMentalUseImpression(long n) { setCustomCounter("on_mental_use_impression", getOnMentalUseImpression() + n); }
    /** 「メンタルスキルカードの元気値増加+N」（インフルエンサー等）：メンタルカード使用のたびに元気+N */
    public long getOnMentalUseGenki() { return getCustomCounter("on_mental_use_genki"); }
    public void addOnMentalUseGenki(long n) { setCustomCounter("on_mental_use_genki", getOnMentalUseGenki() + n); }

    /** アクティブスキルカードのパラメータ値増加（永続、重複可）。「ハッスル」「盛り上げ上手」等 */
    public long getActiveParamBonus() { return getCustomCounter("active_param_bonus"); }
    public void addActiveParamBonus(long n) { setCustomCounter("active_param_bonus", getActiveParamBonus() + n); }

    /** 「次に使用したスキルカードの消費体力を0にする」の残数（「全身全霊」等） */
    public long getFreeCostUses() { return getCustomCounter("free_cost_uses"); }
    public void addFreeCostUses(long n) { setCustomCounter("free_cost_uses", Math.max(0L, getFreeCostUses() + n)); }
    public void consumeFreeCostUse() {
        long c = getFreeCostUses();
        if (c > 0) setCustomCounter("free_cost_uses", c - 1);
    }

    /** このレッスン中に「温存に変更」した回数（「タフネス」の使用条件判定用） */
    public long getConserveActivations() { return getCustomCounter("conserve_activations"); }
    public void incrementConserveActivations() { setCustomCounter("conserve_activations", getConserveActivations() + 1); }

    /**
     * 毎Tick呼び出される時間経過処理。好調・絶好調をリアルタイムで減少させる。
     * 集中は減らさない（本家仕様通り、リセットまで永続）。
     */
    public void tickDown() {
        if (goodConditionTicks > 0) {
            goodConditionTicks--;
            if (goodConditionTicks == 0) {
                goodConditionTurnsAccumulated = 0;
            }
        }
        if (greatConditionTicks > 0) {
            greatConditionTicks--;
        }
    }

    /** デッキリセット時：すべてのバフを完全に0へ戻す */
    public void resetAll() {
        clearStatusBuffs();
        passiveFlags.clear();
        customCounters.clear();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("focus", focusStacks);
        tag.putLong("goodTicks", goodConditionTicks);
        tag.putLong("greatTicks", greatConditionTicks);
        tag.putLong("goodTurnsAcc", goodConditionTurnsAccumulated);
        CompoundTag flagsTag = new CompoundTag();
        int i = 0;
        for (String f : passiveFlags) {
            flagsTag.putString("f" + (i++), f);
        }
        tag.put("flags", flagsTag);
        CompoundTag countersTag = new CompoundTag();
        for (Map.Entry<String, Long> e : customCounters.entrySet()) {
            countersTag.putLong(e.getKey(), e.getValue());
        }
        tag.put("counters", countersTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        focusStacks = tag.getLong("focus");
        goodConditionTicks = tag.getLong("goodTicks");
        greatConditionTicks = tag.getLong("greatTicks");
        goodConditionTurnsAccumulated = tag.getLong("goodTurnsAcc");
        passiveFlags.clear();
        CompoundTag flagsTag = tag.getCompound("flags");
        for (String key : flagsTag.getAllKeys()) {
            passiveFlags.add(flagsTag.getString(key));
        }
        customCounters.clear();
        CompoundTag countersTag = tag.getCompound("counters");
        for (String key : countersTag.getAllKeys()) {
            customCounters.put(key, countersTag.getLong(key));
        }
    }
}
