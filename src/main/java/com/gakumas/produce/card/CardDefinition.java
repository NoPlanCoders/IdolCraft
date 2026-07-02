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
    private final String description;
    private final CardType type;
    private final int hpCost;
    private final int baseScore;
    /** カード使用に要求される進捗（特殊な個別解禁用）。null なら制限なし */
    @Nullable
    private final ResourceLocation requiredAdvancement;
    /** カード使用に要求されるプロデューサーランク（Pレベル）。0 なら制限なし */
    private final int requiredPLevel;
    /** デッキ初期化時のドローで必ず初期手札に含める（「静かな意志」のような初手確定カード） */
    private final boolean guaranteedFirstDraw;
    /** 「集中が3以上の場合、使用可」等の使用条件。満たさない場合は発動そのものをキャンセルする */
    private final CardUsability usability;
    /** 使用条件を満たしていない時にプレイヤーへ表示する説明文（ツールチップ・エラーメッセージ両方で使う） */
    private final String usabilityHint;
    private final CardEffect effect;

    private CardDefinition(Builder b) {
        this.id = b.id;
        this.displayName = b.displayName;
        this.description = b.description;
        this.type = b.type;
        this.hpCost = b.hpCost;
        this.baseScore = b.baseScore;
        this.requiredAdvancement = b.requiredAdvancement;
        this.requiredPLevel = b.requiredPLevel;
        this.guaranteedFirstDraw = b.guaranteedFirstDraw;
        this.usability = b.usability;
        this.usabilityHint = b.usabilityHint;
        this.effect = b.effect;
    }

    public ResourceLocation getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public CardType getType() { return type; }
    public int getHpCost() { return hpCost; }
    public int getBaseScore() { return baseScore; }
    @Nullable
    public ResourceLocation getRequiredAdvancement() { return requiredAdvancement; }
    public int getRequiredPLevel() { return requiredPLevel; }
    public boolean isGuaranteedFirstDraw() { return guaranteedFirstDraw; }
    public CardUsability getUsability() { return usability; }
    public String getUsabilityHint() { return usabilityHint; }
    public CardEffect getEffect() { return effect; }

    public static Builder builder(ResourceLocation id, String displayName, CardType type) {
        return new Builder(id, displayName, type);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final String displayName;
        private final CardType type;
        private String description = "";
        private int hpCost = 0;
        private int baseScore = 0;
        @Nullable
        private ResourceLocation requiredAdvancement = null;
        private int requiredPLevel = 0;
        private boolean guaranteedFirstDraw = false;
        private CardUsability usability = CardUsability.ALWAYS;
        private String usabilityHint = "";
        private CardEffect effect = (player, deck) -> {};

        public Builder(ResourceLocation id, String displayName, CardType type) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
        }

        public Builder description(String description) { this.description = description; return this; }
        public Builder hpCost(int hpCost) { this.hpCost = hpCost; return this; }
        public Builder baseScore(int baseScore) { this.baseScore = baseScore; return this; }
        public Builder requiredAdvancement(@Nullable ResourceLocation adv) { this.requiredAdvancement = adv; return this; }
        public Builder requiredPLevel(int level) { this.requiredPLevel = level; return this; }
        public Builder guaranteedFirstDraw(boolean v) { this.guaranteedFirstDraw = v; return this; }
        /** hint: 「集中3以上が必要」のような、条件未達時にプレイヤーへ表示する短い説明文 */
        public Builder usableWhen(CardUsability usability, String hint) { this.usability = usability; this.usabilityHint = hint; return this; }
        public Builder effect(CardEffect effect) { this.effect = effect; return this; }

        public CardDefinition build() {
            return new CardDefinition(this);
        }
    }
}
