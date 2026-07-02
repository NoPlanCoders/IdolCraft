package com.gakumas.produce.registry;

import com.gakumas.produce.GakumasProduceMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(ForgeRegistries.CREATIVE_MODE_TABS, GakumasProduceMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PRODUCE_TAB = TABS.register("produce_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("学マス風プロデュース"))
                    .icon(() -> new ItemStack(ModItems.PRODUCE_HANDBOOK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.PRODUCE_HANDBOOK.get());
                        output.accept(ModItems.CARD_APPEAL_BASIC.get());
                        output.accept(ModItems.CARD_EXPRESSION_BASIC.get());
                        output.accept(ModItems.CARD_BEHAVIOR_BASIC.get());
                        output.accept(ModItems.CARD_EXPRESSION_FACE_BASIC.get());
                        output.accept(ModItems.CARD_QUIET_WILL.get());
                        output.accept(ModItems.CARD_DIRECTION_PLAN.get());
                    })
                    .build());

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        TABS.register(bus);
    }
}
