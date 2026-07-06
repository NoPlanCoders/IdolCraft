package com.idolcraft.util;

import com.idolcraft.buff.BuffState;

/**
 * 本家学マスの計算式を踏襲したダメージ/スコア計算。
 *
 * 集中：スコアへの加算値。
 * 好調：(スコア+集中) に 1.5倍 の倍率をかける。
 * 絶好調：好調がアクティブな場合のみ効果を発揮し、「好調のスコア上昇50%の値に、
 *        好調が付与されたターン数×10%を加算」した倍率に置き換わる
 *        （本家Wiki準拠の式: 1 + 0.5 + 0.1×好調ターン数）。
 *        好調が無い状態では絶好調単体では何も倍率を上げない（本家仕様）。
 * 端数は切り上げ。
 *
 * スコアの内部計算はすべて long で行い、高倍率・大量スタック時の int オーバーフローを避ける。
 * Minecraft のエンティティ体力・{@code hurt()} は float のため、最終的にエンティティへ
 * 与えるダメージだけ {@link #toEntityDamage(long)} で float へクランプして渡す。
 */
public final class ScoreMath {

    private ScoreMath() {}

    /** バフ適用後のスコアを long で返す（オーバーフローを避けた内部計算値）。 */
    public static long calculateScore(long baseScore, BuffState buff, double goodConditionMultiplier) {
        return (long) Math.ceil(applyBuffs(baseScore, buff, goodConditionMultiplier));
    }

    public static long calculateScore(long baseScore, BuffState buff) {
        return calculateScore(baseScore, buff, 1.0);
    }

    /** エンティティへ与えるダメージ（float）。long 計算値を float へクランプして返す。 */
    public static float calculateDamage(long baseScore, BuffState buff, double goodConditionMultiplier) {
        return toEntityDamage(calculateScore(baseScore, buff, goodConditionMultiplier));
    }

    public static float calculateDamage(long baseScore, BuffState buff) {
        return calculateDamage(baseScore, buff, 1.0);
    }

    public static float calculateOutgoingDamage(float baseDamage, BuffState buff) {
        long score = (long) Math.ceil(applyBuffs(baseDamage, buff, 1.0));
        return toEntityDamage(score);
    }

    /** long のスコア値を hurt() に渡せる float ダメージへ変換する（非負・float上限でクランプ）。 */
    public static float toEntityDamage(long score) {
        if (score <= 0L) return 0f;
        return (float) Math.min(score, (long) Float.MAX_VALUE);
    }

    private static double applyBuffs(double baseValue, BuffState buff, double goodConditionMultOverride) {
        double raw = baseValue + buff.getFocusStacks();
        return raw * getDamageMultiplier(buff, goodConditionMultOverride);
    }

    private static double getDamageMultiplier(BuffState buff, double customGoodMult) {
        if (buff.isGoodConditionActive()) {
            if (buff.isGreatConditionActive()) {
                return 1.0 + 0.5 + 0.1 * buff.getGoodConditionTurnsAccumulated();
            }
            return (customGoodMult > 0) ? customGoodMult : 1.5;
        }
        return 1.0;
    }

    // 好調効果2倍（ステージングの基本等）用の定数
    public static final double GOOD_MULT_DOUBLE = 2.0;
}
