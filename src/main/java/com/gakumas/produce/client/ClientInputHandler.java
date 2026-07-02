package com.gakumas.produce.client;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.network.NetworkHandler;
import com.gakumas.produce.network.packet.ResetDeckPacket;
import com.gakumas.produce.network.packet.SelectCardPacket;
import com.gakumas.produce.network.packet.UseOrSkipPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientInputHandler {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.RESET_DECK);
        event.register(KeyBindings.SKIP_CARD);
    }

    @Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            ClientDeckState.localTick();

            while (KeyBindings.RESET_DECK.consumeClick()) {
                NetworkHandler.CHANNEL.sendToServer(new ResetDeckPacket());
            }
            while (KeyBindings.SKIP_CARD.consumeClick()) {
                if (isHoldingHandbook()) {
                    NetworkHandler.CHANNEL.sendToServer(new UseOrSkipPacket(true));
                }
            }
        }

        /**
         * スニーク + ホイールスクロールで手札選択カーソルを移動する（仕様2手順1）。
         */
        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!mc.player.isShiftKeyDown()) return; // スニーク中のみ
            if (!isHoldingHandbook()) return;

            double delta = event.getScrollDeltaY();
            if (delta == 0) return;
            int dir = delta > 0 ? -1 : 1; // 上スクロールで前のカード、下スクロールで次のカード
            NetworkHandler.CHANNEL.sendToServer(new SelectCardPacket(dir));
            event.setCanceled(true); // スニーク中はホットバー切替を防止
        }

        private static boolean isHoldingHandbook() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return false;
            return mc.player.getMainHandItem().getItem() instanceof HandbookItem
                    || mc.player.getOffhandItem().getItem() instanceof HandbookItem;
        }
    }
}
