package com.gakumas.produce.registry;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GakumasProduceMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PRODUCE_TAB = TABS.register("produce_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("学マス風プロデュース"))
                    .icon(() -> new ItemStack(ModItems.PRODUCE_HANDBOOK.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.PRODUCE_HANDBOOK.get());
                        output.accept(ModItems.CARD_WORKSHOP.get());
                        output.accept(ModItems.MATERIAL_WHITE.get());
                        output.accept(ModItems.MATERIAL_SILVER.get());
                        output.accept(ModItems.MATERIAL_GOLD.get());
                        output.accept(ModItems.MATERIAL_RAINBOW.get());
                        // CardRegistry に登録された全カードを自動的にタブへ並べる。
                        // 新カード追加時にここを書き換える必要がなくなる（拡張性重視）。
                        for (CardDefinition def : CardRegistry.all()) {
                            Item item = ForgeRegistries.ITEMS.getValue(def.getId());
                            if (item != null) {
                                output.accept(item);
                            }
                        }
                    })
                    .build());

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        TABS.register(bus);
    }
}
