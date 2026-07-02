package com.gakumas.produce.card;

/**
 * 学マス本家のカード種別。
 * NORMAL = 通常（使用後は捨て札へ戻り、山札シャッフルで再登場する）
 * ONCE_PER_LESSON = 「レッスン中1回」（使用後は捨て札ではなく除外へ、そのレッスン中は二度と引かない）
 */
public enum CardType {
    NORMAL,
    ONCE_PER_LESSON
}
