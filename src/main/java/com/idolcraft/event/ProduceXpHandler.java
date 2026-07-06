package com.idolcraft.event;

import com.idolcraft.IdolCraft;
import com.idolcraft.capability.DeckCapability;
import com.idolcraft.network.SyncHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * プロデューサーランク（Pレベル）の経験値制の進行を管理するハンドラー。
 *
 * プレイヤーが経験値オーブを取得するたびに、その取得量をPレベル用の累計経験値に加算する。
 * Minecraft本来の経験値レベルは消費せずに温存する（エンチャント等はそのまま使える）。
 * 累計経験値が閾値を超えるとPレベルが上昇し、より高いランクのカードが解放される。
 */
@Mod.EventBusSubscriber(modid = IdolCraft.MOD_ID)
public final class ProduceXpHandler {

    private ProduceXpHandler() {}

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int gained = event.getOrb().getValue();
        if (gained <= 0) return;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            int before = deck.getPLevel();
            deck.addProduceXp(gained);
            int after = deck.getPLevel();

            if (after > before) {
                player.displayClientMessage(
                        Component.literal("【プロデューサーランクが Lv." + after + " に上昇！】")
                                .withStyle(ChatFormatting.GOLD),
                        false
                );
            }
            // 進捗バー・解放状況の同期（レベルが変わらなくてもXPバー表示のため同期する）
            SyncHelper.syncTo(player, deck);
        });
    }
}

