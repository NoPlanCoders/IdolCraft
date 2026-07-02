package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
public class SleepHandler {

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        // updateWorld = trueの場合のみ「朝を迎えて時間が進んだ（＝実際に夜を明かした）」ケースとして扱う
        if (!event.updateWorld()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.resetDeck(player, deck);
            SyncHelper.syncTo(player, deck);
        });
    }
}
