package com.gakumas.produce.buff;

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
 */
public class BuffState {

    /** 集中スタック数（永続。1ターン=5秒換算の時間経過では減らない） */
    private int focusStacks = 0;

    /** 好調の残りTick数（20Tick=1秒。リアルタイムで毎Tick減少） */
    private int goodConditionTicks = 0;

    /** 絶好調の残りTick数（同上） */
    private int greatConditionTicks = 0;

    /**
     * 好調が「今回のバフ期間中」に延べ何ターン分付与されたかを保持するカウンタ。
     * 学マス本家の絶好調計算式（好調のターン数に応じて倍率が変わる）を再現するために使用する。
     * 好調が完全に0になった時点で0にリセットする。
     */
    private int goodConditionTurnsAccumulated = 0;

    /** 汎用パッシブ効果フラグ（例: "encore_genki_on_use" = 演出計画のカード使用時元気+2） */
    private final Set<String> passiveFlags = new HashSet<>();

    /** 拡張用の汎用整数値ストレージ。将来の新バフ・新プランで自由に使える */
    private final Map<String, Integer> customCounters = new HashMap<>();

    public int getFocusStacks() {
        return focusStacks;
    }

    public void addFocus(int amount) {
        this.focusStacks = Math.max(0, this.focusStacks + amount);
    }

    public int getGoodConditionTicks() {
        return goodConditionTicks;
    }

    public boolean isGoodConditionActive() {
        return goodConditionTicks > 0;
    }

    /**
     * 好調を付与する。学マス仕様同様、重複付与は「持続時間の延長」として扱う（上書きではなく加算）。
     * 1ターン=5秒=100Tick換算で呼び出し側から渡されたTick数をそのまま加算する。
     */
    public void addGoodCondition(int ticks, int turns) {
        this.goodConditionTicks += ticks;
        this.goodConditionTurnsAccumulated += turns;
    }

    public int getGreatConditionTicks() {
        return greatConditionTicks;
    }

    public boolean isGreatConditionActive() {
        return greatConditionTicks > 0;
    }

    public void addGreatCondition(int ticks) {
        this.greatConditionTicks += ticks;
    }

    /** 集中・好調・絶好調だけをまとめて消す。進捗やパッシブフラグは残す。 */
    public void clearStatusBuffs() {
        focusStacks = 0;
        goodConditionTicks = 0;
        greatConditionTicks = 0;
        goodConditionTurnsAccumulated = 0;
    }

    public int getGoodConditionTurnsAccumulated() {
        return goodConditionTurnsAccumulated;
    }

    public boolean hasPassiveFlag(String flag) {
        return passiveFlags.contains(flag);
    }

    public void setPassiveFlag(String flag, boolean value) {
        if (value) passiveFlags.add(flag);
        else passiveFlags.remove(flag);
    }

    public int getCustomCounter(String key) {
        return customCounters.getOrDefault(key, 0);
    }

    public void setCustomCounter(String key, int value) {
        customCounters.put(key, value);
    }

    /** 「スキルカード使用数追加」用カウンタキー。ターンを終えず続けてもう1枚使えるボーナスの残数。 */
    private static final String BONUS_ACTION_KEY = "bonus_actions";

    public int getBonusActions() {
        return getCustomCounter(BONUS_ACTION_KEY);
    }

    /** 「シュプレヒコール」等：このカード使用後、ターンを終えずもう1枚使用できるボーナスを加算する */
    public void addBonusAction(int amount) {
        setCustomCounter(BONUS_ACTION_KEY, Math.max(0, getBonusActions() + amount));
    }

    /** ボーナスを1消費する（残数が無ければ何もしない） */
    public void consumeBonusAction() {
        int current = getBonusActions();
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
    public int getPendingDraw() { return getCustomCounter(PENDING_DRAW_KEY); }
    public void addPendingDraw(int n) { setCustomCounter(PENDING_DRAW_KEY, Math.max(0, getPendingDraw() + n)); }
    public void clearPendingDraw() { setCustomCounter(PENDING_DRAW_KEY, 0); }

    // ── 消費体力減少（残ターン数。>0 の間、カードの消費体力を軽減する） ──
    private static final String COST_REDUCTION_KEY = "cost_reduction_turns";
    public int getCostReductionTurns() { return getCustomCounter(COST_REDUCTION_KEY); }
    public void addCostReductionTurns(int t) { setCustomCounter(COST_REDUCTION_KEY, Math.max(0, getCostReductionTurns() + t)); }
    public void tickCostReductionTurn() {
        int c = getCostReductionTurns();
        if (c > 0) setCustomCounter(COST_REDUCTION_KEY, c - 1);
    }

    // ── 次に使うカードの効果を2回発動する残数（「国民的アイドル」等） ──
    private static final String DOUBLE_NEXT_KEY = "double_next_cards";
    public int getDoubleNextCards() { return getCustomCounter(DOUBLE_NEXT_KEY); }
    public void addDoubleNextCards(int n) { setCustomCounter(DOUBLE_NEXT_KEY, Math.max(0, getDoubleNextCards() + n)); }
    public void consumeDoubleNextCard() {
        int c = getDoubleNextCards();
        if (c > 0) setCustomCounter(DOUBLE_NEXT_KEY, c - 1);
    }

    // ── 継続パラメータ（ターン終了毎にこの値ぶんターゲットへダメージ。「至高のエンタメ」等） ──
    private static final String PARAM_PER_TURN_KEY = "param_per_turn";
    public int getParamPerTurn() { return getCustomCounter(PARAM_PER_TURN_KEY); }
    public void addParamPerTurn(int n) { setCustomCounter(PARAM_PER_TURN_KEY, Math.max(0, getParamPerTurn() + n)); }

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
        tag.putInt("focus", focusStacks);
        tag.putInt("goodTicks", goodConditionTicks);
        tag.putInt("greatTicks", greatConditionTicks);
        tag.putInt("goodTurnsAcc", goodConditionTurnsAccumulated);
        CompoundTag flagsTag = new CompoundTag();
        int i = 0;
        for (String f : passiveFlags) {
            flagsTag.putString("f" + (i++), f);
        }
        tag.put("flags", flagsTag);
        CompoundTag countersTag = new CompoundTag();
        for (Map.Entry<String, Integer> e : customCounters.entrySet()) {
            countersTag.putInt(e.getKey(), e.getValue());
        }
        tag.put("counters", countersTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        focusStacks = tag.getInt("focus");
        goodConditionTicks = tag.getInt("goodTicks");
        greatConditionTicks = tag.getInt("greatTicks");
        goodConditionTurnsAccumulated = tag.getInt("goodTurnsAcc");
        passiveFlags.clear();
        CompoundTag flagsTag = tag.getCompound("flags");
        for (String key : flagsTag.getAllKeys()) {
            passiveFlags.add(flagsTag.getString(key));
        }
        customCounters.clear();
        CompoundTag countersTag = tag.getCompound("counters");
        for (String key : countersTag.getAllKeys()) {
            customCounters.put(key, countersTag.getInt(key));
        }
    }
}
