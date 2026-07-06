package com.idolcraft.event;

import com.idolcraft.IdolCraft;
import com.idolcraft.capability.DeckCapability;
import com.idolcraft.capability.DeckService;
import com.idolcraft.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IdolCraft.MOD_ID)
public class SleepHandler {

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        // updateWorld = trueの場合のみ「朝を迎えて時間が進んだ（＝実際に夜を明かした）」ケースとして扱う
        if (!event.updateLevel()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.resetDeck(player, deck);
            SyncHelper.syncTo(player, deck);
        });
    }
}

