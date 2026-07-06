package com.idolcraft.capability;

import com.idolcraft.buff.BuffState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * プレイヤー1人分の「デッキ」（山札・手札・捨て札・除外）とバフ状態を保持するCapabilityインターフェース。
 *
 * 山札/手札/捨て札/除外はいずれも ResourceLocation（カードアイテムのregistry name）のリストとして保持する。
 * カードの実際の効果・コスト等は {@link com.idolcraft.card.CardRegistry} から都度引く設計にすることで、
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
     * 実体は {@link #getProduceXp() 累計経験値} から {@link com.idolcraft.util.PLevelCurve} で導出される。
     */
    int getPLevel();
    /** Pレベルを直接指定する（対応する累計経験値に置き換える）。コマンド等のデバッグ用途向け。 */
    void setPLevel(int level);

    /** Pレベルの基準となる累計経験値（Minecraftの経験値オーブ取得量の累計） */
    long getProduceXp();
    void setProduceXp(long xp);
    /** 累計経験値を加算する（負値は0でクランプ）。 */
    void addProduceXp(long delta);

    /**
     * 「習得済み（入手済み）」カードのコレクション。デッキ編成はこの集合の中からのみ行える。
     * カードアイテムを右クリックで習得すると恒久的に追加される（アイテム紛失や死亡では失われない）。
     */
    java.util.Set<ResourceLocation> getOwnedCards();
    /** カードを習得済みに加える。新規に追加された場合のみ true を返す。 */
    boolean addOwnedCard(ResourceLocation cardId);
    boolean hasOwnedCard(ResourceLocation cardId);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}

