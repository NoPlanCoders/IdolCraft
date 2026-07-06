package com.idolcraft.card;

import com.idolcraft.capability.IDeckData;
import net.minecraft.server.level.ServerPlayer;

/**
 * カードが「今この瞬間に選択・発動できるか」を判定する条件。
 * 「集中が3以上の場合、使用可」のような本家の使用条件付きカードを表現するために使う。
 * 条件を満たさない場合、{@link com.idolcraft.capability.DeckService} はカード発動そのものを
 * キャンセルしメッセージを表示する（黙って何も起きない「不発」を避けるため）。
 */
@FunctionalInterface
public interface CardUsability {
    boolean canUse(ServerPlayer player, IDeckData deck);

    CardUsability ALWAYS = (player, deck) -> true;
}

