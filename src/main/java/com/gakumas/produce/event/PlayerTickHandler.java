package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 好調/絶好調はカード使用回数ではなく「リアルタイムのTick経過」で厳密に管理する（仕様5）。
 * そのため PlayerTickEvent で毎Tick減少させる。
 */
@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
public class PlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        event.player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().tickDown();

            // 手帳を所持している間のみ、5Tickに1回HUD用に同期する（毎Tick送ると負荷が高いため）
            boolean holdingHandbook = serverPlayer.getMainHandItem().getItem() instanceof HandbookItem
                    || serverPlayer.getOffhandItem().getItem() instanceof HandbookItem;
            boolean hasVisibleBuff = deck.getBuffState().getFocusStacks() > 0
                    || deck.getBuffState().isGoodConditionActive()
                    || deck.getBuffState().isGreatConditionActive();
            if ((holdingHandbook || hasVisibleBuff) && serverPlayer.tickCount % 5 == 0) {
                SyncHelper.syncTo(serverPlayer, deck);
            }
        });
    }
}
