package com.gakumas.produce.util;

import com.gakumas.produce.buff.BuffState;

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

    public static int calculateDamage(int baseScore, BuffState buff) {
        int raw = baseScore + buff.getFocusStacks();

        double multiplier = 1.0;
        if (buff.isGoodConditionActive()) {
            if (buff.isGreatConditionActive()) {
                multiplier = 1.0 + 0.5 + 0.1 * buff.getGoodConditionTurnsAccumulated();
            } else {
                multiplier = 1.5;
            }
        }
        return (int) Math.ceil(raw * multiplier);
    }
}
