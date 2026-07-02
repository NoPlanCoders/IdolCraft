package com.gakumas.produce.card;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * カード1種類分の静的な定義データ。
 * 実際のゲームプレイ上の「カード実体」はデッキ内での ResourceLocation（アイテムのregistry name）で
 * 参照され、このクラスはその registry name に対応する不変のルールセットを保持する。
 *
 * 新カードを追加する場合は、対応する Item を1つ登録して
 * {@link CardRegistry#register(CardDefinition)} を1行呼ぶだけでよい。
 */
public class CardDefinition {

    private final ResourceLocation id;
    private final String displayName;
    private final CardType type;
    private final int hpCost;
    private final int baseScore;
    /** カード使用に要求される進捗（プロデューサーランク）。null なら制限なし */
    @Nullable
    private final ResourceLocation requiredAdvancement;
    /** デッキ初期化時のドローで必ず初期手札に含める（「静かな意志」のような初手確定カード） */
    private final boolean guaranteedFirstDraw;
    private final CardEffect effect;

    private CardDefinition(Builder b) {
        this.id = b.id;
        this.displayName = b.displayName;
        this.type = b.type;
        this.hpCost = b.hpCost;
        this.baseScore = b.baseScore;
        this.requiredAdvancement = b.requiredAdvancement;
        this.guaranteedFirstDraw = b.guaranteedFirstDraw;
        this.effect = b.effect;
    }

    public ResourceLocation getId() { return id; }
    public String getDisplayName() { return displayName; }
    public CardType getType() { return type; }
    public int getHpCost() { return hpCost; }
    public int getBaseScore() { return baseScore; }
    @Nullable
    public ResourceLocation getRequiredAdvancement() { return requiredAdvancement; }
    public boolean isGuaranteedFirstDraw() { return guaranteedFirstDraw; }
    public CardEffect getEffect() { return effect; }

    public static Builder builder(ResourceLocation id, String displayName, CardType type) {
        return new Builder(id, displayName, type);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final String displayName;
        private final CardType type;
        private int hpCost = 0;
        private int baseScore = 0;
        @Nullable
        private ResourceLocation requiredAdvancement = null;
        private boolean guaranteedFirstDraw = false;
        private CardEffect effect = (player, deck) -> {};

        public Builder(ResourceLocation id, String displayName, CardType type) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
        }

        public Builder hpCost(int hpCost) { this.hpCost = hpCost; return this; }
        public Builder baseScore(int baseScore) { this.baseScore = baseScore; return this; }
        public Builder requiredAdvancement(@Nullable ResourceLocation adv) { this.requiredAdvancement = adv; return this; }
        public Builder guaranteedFirstDraw(boolean v) { this.guaranteedFirstDraw = v; return this; }
        public Builder effect(CardEffect effect) { this.effect = effect; return this; }

        public CardDefinition build() {
            return new CardDefinition(this);
        }
    }
}
