package com.gakumas.produce;

import com.gakumas.produce.card.impl.ProduceCards;
import com.gakumas.produce.network.NetworkHandler;
import com.gakumas.produce.registry.ModBlocks;
import com.gakumas.produce.registry.ModCreativeTab;
import com.gakumas.produce.registry.ModItems;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GakumasProduceMod.MOD_ID)
public class GakumasProduceMod {

    public static final String MOD_ID = "gakumas_produce";

    public GakumasProduceMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModCreativeTab.register(modBus);

        modBus.addListener(this::commonSetup);
        // event.CapabilityEvents / PlayerTickHandler / InteractionHandler / SleepHandler は
        // @Mod.EventBusSubscriber により自動登録されるため、ここで明示的な登録は不要。
        // ClientSetup / ClientInputHandler も同様（Dist.CLIENT指定のため物理クライアントのみロードされる）。
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ProduceCards.registerAll();
            com.gakumas.produce.card.impl.ProduceSenseCards.registerAll();
            NetworkHandler.register();
        });
    }
}
