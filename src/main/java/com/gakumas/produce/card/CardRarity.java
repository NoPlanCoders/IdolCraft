package com.gakumas.produce.card;

/**
 * スキルカードのレア度（本家学マスの白/銀/金/虹）。
 * レア度別カードパックの抽選や表示色に使用する。
 */
public enum CardRarity {
    WHITE("白", 0xFFBFC4D0),
    SILVER("銀", 0xFFAEC0D8),
    GOLD("金", 0xFFE8C25A),
    RAINBOW("虹", 0xFFE466B0);

    private final String label;
    private final int color;

    CardRarity(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public int getColor() { return color; }
}
