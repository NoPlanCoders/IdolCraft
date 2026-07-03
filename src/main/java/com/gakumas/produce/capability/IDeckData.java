package com.gakumas.produce.capability;

import com.gakumas.produce.buff.BuffState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * プレイヤー1人分の「デッキ」（山札・手札・捨て札・除外）とバフ状態を保持するCapabilityインターフェース。
 *
 * 山札/手札/捨て札/除外はいずれも ResourceLocation（カードアイテムのregistry name）のリストとして保持する。
 * カードの実際の効果・コスト等は {@link com.gakumas.produce.card.CardRegistry} から都度引く設計にすることで、
 * 将来カードを追加してもこのCapability自体の構造を変える必要がない。
 */
public interface IDeckData {

    List<ResourceLocation> getDrawPile();
    List<ResourceLocation> getHand();
    List<ResourceLocation> getDiscardPile();
    List<ResourceLocation> getExclusionPile();

    /** 現在手札内で選択されているインデックス (0-2)。未選択時は -1 は使わず常に範囲内にクランプする */
    int getSelectedIndex();
    void setSelectedIndex(int index);

    BuffState getBuffState();

    /** そのプロデュース周期(デッキが有効な間)に登録されているデッキの全カード構成（リセット時の復元用マスターリスト） */
    List<ResourceLocation> getMasterCardList();
    void setMasterCardList(List<ResourceLocation> cards);

    /** このデッキがまだ一度も初期化(ドロー)されていないかどうか */
    boolean isInitialized();
    void setInitialized(boolean initialized);

    /**
     * プロデューサーランク（Pレベル）。デッキリセットやバフリセットでは変化しない、
     * プレイヤーの長期的な進捗値。この値によって使用可能なカードの種類が増えていく。
     * 実体は {@link #getProduceXp() 累計経験値} から {@link com.gakumas.produce.util.PLevelCurve} で導出される。
     */
    int getPLevel();
    /** Pレベルを直接指定する（対応する累計経験値に置き換える）。コマンド等のデバッグ用途向け。 */
    void setPLevel(int level);

    /** Pレベルの基準となる累計経験値（Minecraftの経験値オーブ取得量の累計） */
    long getProduceXp();
    void setProduceXp(long xp);
    /** 累計経験値を加算する（負値は0でクランプ）。 */
    void addProduceXp(long delta);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
