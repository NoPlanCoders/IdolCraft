package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * 「プロデューサーランク（Pレベル）」の進行を管理するハンドラー。
 * data/gakumas_produce/advancements/rank/ 以下の進捗チェーンの達成を監視し、
 * 対応するPレベルにプレイヤーの値を引き上げる。
 *
 * 進捗ID → Pレベルの対応表を差し替えるだけで解禁段階を追加・調整できるようにしてある
 * （本家学マスの「PLv6」「PLv11」...のような解放段階を、Minecraft側は進捗チェーンとして実装）。
 */
@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
public class PLevelHandler {

    private static final Map<ResourceLocation, Integer> ADVANCEMENT_TO_PLEVEL = Map.of(
            id("rank/plv_6"), 6,
            id("rank/plv_11"), 11,
            id("rank/upper"), 20,
            id("rank/plv_24"), 24,
            id("rank/plv_25"), 25
    );

    private static ResourceLocation id(String path) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, path);
    }

    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Advancement advancement = event.getAdvancement();
        Integer newLevel = ADVANCEMENT_TO_PLEVEL.get(advancement.getId());
        if (newLevel == null) return;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            if (newLevel <= deck.getPLevel()) return; // 既にそれ以上のランクなら何もしない

            deck.setPLevel(newLevel);
            player.displayClientMessage(
                    Component.literal("【プロデューサーランクが Lv." + newLevel + " に上昇！】")
                            .withStyle(ChatFormatting.GOLD),
                    false
            );
            SyncHelper.syncTo(player, deck);
        });
    }
}
