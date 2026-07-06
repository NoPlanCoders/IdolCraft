package com.idolcraft.util;

/**
 * プロデューサーランク（Pレベル）の経験値カーブ。サーバー・クライアント双方で共有する。
 *
 * Pレベルは獲得した経験値（Minecraftの経験値オーブ取得量の累計）で上昇し、
 * 次のレベルに必要な量はレベルが上がるごとに逓増する。
 */
public final class PLevelCurve {

    private PLevelCurve() {}

    /** 到達可能な最大Pレベル（本家の解放カードは最大PLv56のためそれ以上に余裕を持たせる） */
    public static final int MAX_LEVEL = 80;

    /**
     * レベル {@code level} から {@code level+1} へ上がるのに必要な経験値。
     * Lv1→2 は 20、以降レベルが上がるごとに +10 ずつ増える（逓増）。
     */
    public static long costToNext(int level) {
        return 20L + 10L * Math.max(0, level - 1);
    }

    /** レベル {@code level} に「到達」するのに必要な累計経験値 */
    public static long totalXpForLevel(int level) {
        long sum = 0;
        for (int l = 1; l < level; l++) sum += costToNext(l);
        return sum;
    }

    /** 累計経験値 {@code xp} に対応する現在のPレベル（最小1、最大 {@link #MAX_LEVEL}） */
    public static int levelForXp(long xp) {
        int level = 1;
        long acc = 0;
        while (level < MAX_LEVEL) {
            long cost = costToNext(level);
            if (xp >= acc + cost) {
                acc += cost;
                level++;
            } else {
                break;
            }
        }
        return level;
    }

    /** 現在レベル内での経験値進捗（現在の累計から、現在レベル到達時点の累計を引いた値） */
    public static long xpIntoLevel(long xp) {
        int level = levelForXp(xp);
        return xp - totalXpForLevel(level);
    }

    /** 現在レベルから次のレベルへ上がるのに必要な経験値（最大レベルなら0） */
    public static long xpForNext(long xp) {
        int level = levelForXp(xp);
        return level >= MAX_LEVEL ? 0 : costToNext(level);
    }

    public static boolean isMaxLevel(long xp) {
        return levelForXp(xp) >= MAX_LEVEL;
    }
}

