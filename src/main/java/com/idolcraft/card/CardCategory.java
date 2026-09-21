package com.idolcraft.card;

/**
 * 本家学マスの「アクティブ／メンタル」区分（wikiの type: A/M に対応）。
 * 「アクティブスキルカード使用時」「メンタルスキルカード使用時」といった、
 * カード種別を条件にする一部のパッシブ効果の判定に使う。
 */
public enum CardCategory {
    ACTIVE("A"),
    MENTAL("M");

    private final String wikiCode;

    CardCategory(String wikiCode) {
        this.wikiCode = wikiCode;
    }

    public String getWikiCode() {
        return wikiCode;
    }
}
