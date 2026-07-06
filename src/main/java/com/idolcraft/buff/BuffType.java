package com.idolcraft.buff;

/**
 * センスプラン（集中・好調・絶好調）のバフ種別。
 * 将来「ロジック」プラン（やる気・好印象等）を追加する際は、
 * ここに列挙値を追加するだけで {@link BuffState} や HUD 描画側の共通処理に乗せられるように設計している。
 */
public enum BuffType {
    /**
     * 集中：時間経過では減少しない永続スタック型バフ。リセットされるまで蓄積し続ける。
     * ダメージ計算では加算値として扱われる。
     */
    FOCUS("focus", Mode.STACK),

    /**
     * 好調：リアルタイム秒数で管理される持続時間型バフ。アクティブな間、スコア倍率1.5倍。
     */
    GOOD_CONDITION("good_condition", Mode.DURATION),

    /**
     * 絶好調：リアルタイム秒数で管理される持続時間型バフ。好調と重複してアクティブな場合のみ効果を発揮する。
     */
    GREAT_CONDITION("great_condition", Mode.DURATION);

    /** バフの管理方式。STACK=永続加算、DURATION=リアルタイム時間経過で減少。 */
    public enum Mode { STACK, DURATION }

    private final String id;
    private final Mode mode;

    BuffType(String id, Mode mode) {
        this.id = id;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public Mode getMode() {
        return mode;
    }
}

