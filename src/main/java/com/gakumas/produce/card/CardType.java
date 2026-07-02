package com.gakumas.produce.card;

/**
 * 学マス本家のカード種別。
 * L = 制限なし（使用後は捨て札へ）
 * O = 「レッスン中1回」相当（使用後は捨て札ではなく除外へ、リセットまで再登場しない）
 */
public enum CardType {
    L_CARD,
    O_CARD
}
