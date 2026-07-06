package com.idolcraft.client;

import com.idolcraft.IdolCraft;
import com.idolcraft.client.gui.GakumasHudOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IdolCraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("gakumas_hud", new GakumasHudOverlay());
    }
}

