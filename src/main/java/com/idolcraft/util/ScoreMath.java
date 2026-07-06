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
 */
public final class ScoreMath {

    private ScoreMath() {}

    public static int calculateDamage(int baseScore, BuffState buff, double goodConditionMultiplier) {
        return (int) Math.ceil(applyBuffs(baseScore, buff, goodConditionMultiplier));
    }

    public static int calculateDamage(int baseScore, BuffState buff) {
        return calculateDamage(baseScore, buff, 1.0);
    }

    public static float calculateOutgoingDamage(float baseDamage, BuffState buff) {
        return (float) Math.ceil(applyBuffs(baseDamage, buff, 1.0));
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

