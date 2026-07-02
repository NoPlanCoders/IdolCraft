package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 手帳を持った状態での右クリック = 選択中カードの発動（仕様2 手順2）。
 */
@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
public class InteractionHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getItemStack().getItem() instanceof HandbookItem)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return; // クライアント側の重複呼び出しを避ける
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.performAction(player, deck, false);
            SyncHelper.syncTo(player, deck);
        });
    }
}
