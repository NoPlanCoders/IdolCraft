package com.idolcraft;

import com.idolcraft.card.impl.ProduceCards;
import com.idolcraft.network.NetworkHandler;
import com.idolcraft.registry.ModBlocks;
import com.idolcraft.registry.ModCreativeTab;
import com.idolcraft.registry.ModItems;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IdolCraft.MOD_ID)
public class IdolCraft {

    public static final String MOD_ID = "idolcraft";

    public IdolCraft() {
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
            com.idolcraft.card.impl.ProduceSenseCards.registerAll();
            NetworkHandler.register();
        });
    }
}

