package com.gakumas.produce.registry;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.item.ProduceCardItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GakumasProduceMod.MOD_ID);

    public static final RegistryObject<Item> PRODUCE_HANDBOOK = ITEMS.register("produce_handbook",
            () -> new HandbookItem(new Item.Properties().stacksTo(1)));

    // ---- 初期実装6カード ----
    public static final RegistryObject<Item> CARD_APPEAL_BASIC = ITEMS.register("card_appeal_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_EXPRESSION_BASIC = ITEMS.register("card_expression_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_BEHAVIOR_BASIC = ITEMS.register("card_behavior_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_EXPRESSION_FACE_BASIC = ITEMS.register("card_facial_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_QUIET_WILL = ITEMS.register("card_quiet_will",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_DIRECTION_PLAN = ITEMS.register("card_direction_plan",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ITEMS.register(bus);
    }
}
