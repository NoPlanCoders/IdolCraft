package com.idolcraft.card;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * カード定義の中央レジストリ。
 * アイテムの registry name（= カードID）から {@link CardDefinition} を引けるようにする。
 * 新規カード追加時はここに1行追加するだけでよく、デッキシステム本体・HUD・ダメージ計算等は
 * このレジストリ経由で汎用的に動作するため変更不要。
 */
public final class CardRegistry {

    private static final Map<ResourceLocation, CardDefinition> CARDS = new LinkedHashMap<>();

    private CardRegistry() {}

    public static void register(CardDefinition definition) {
        CARDS.put(definition.getId(), definition);
    }

    public static Optional<CardDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(CARDS.get(id));
    }

    public static Collection<CardDefinition> all() {
        return CARDS.values();
    }
}

