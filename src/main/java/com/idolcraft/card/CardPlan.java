package com.idolcraft.card;

/**
 * 本家学マスの「プラン」区分。デッキ編成はプラン単位で行う（フリーは全プラン共通で編成に使える）。
 */
public enum CardPlan {
    FREE("フリー"),
    SENSE("センス"),
    LOGIC("ロジック"),
    ANOMALY("アノマリー");

    private final String label;

    CardPlan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** このプランのデッキにこのカードを組み込めるか（フリーはどのプランにも組み込める） */
    public boolean isCompatibleWith(CardPlan deckPlan) {
        return this == FREE || this == deckPlan;
    }
}
