package com.gakumas.produce.card;

import com.gakumas.produce.capability.IDeckData;
import net.minecraft.server.level.ServerPlayer;

/**
 * カードの効果本体を表す関数型インターフェース。
 * 「センス」プランに限らず、将来「ロジック」プランなど全く異なる効果系統を
 * 追加する場合も、このインターフェースを実装するだけで {@link CardDefinition} に
 * 組み込めるように設計している（ダメージ計算式やバフ付与ロジックをこのMOD内部の
 * 特定クラスに固定しないため、CardEffect の実装クラス側に処理を閉じ込めている）。
 */
@FunctionalInterface
public interface CardEffect {
    /**
     * カード発動時の処理本体。
     * @param player  発動したプレイヤー
     * @param deck    プレイヤーのデッキ/バフデータ
     */
    void apply(ServerPlayer player, IDeckData deck);
}
