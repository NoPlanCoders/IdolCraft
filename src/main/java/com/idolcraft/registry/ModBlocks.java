package com.idolcraft.registry;

import com.idolcraft.IdolCraft;
import com.idolcraft.block.CardWorkshopBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IdolCraft.MOD_ID);

    /** スキルカード作業台：右クリックでカード解放GUIを開く */
    public static final RegistryObject<Block> CARD_WORKSHOP = BLOCKS.register("card_workshop",
            () -> new CardWorkshopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .strength(2.0f)
                    .noOcclusion()));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BLOCKS.register(bus);
    }
}

