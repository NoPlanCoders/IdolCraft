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

    // ---- 高性能カード ----
    public static final RegistryObject<Item> CARD_SHOFU_HIKO = ITEMS.register("card_shofu_hiko",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_EXISTENCE = ITEMS.register("card_existence",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FASCINATION = ITEMS.register("card_fascination",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_INNOCENCE = ITEMS.register("card_innocence",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_CALL_RESPONSE = ITEMS.register("card_call_response",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_EXCITE = ITEMS.register("card_excite",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    // ---- センス基本カード / フリー基本カード ----
    public static final RegistryObject<Item> CARD_STAGING_BASIC = ITEMS.register("card_staging_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_STEP_BASIC = ITEMS.register("card_step_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_PERFORMANCE_BASIC = ITEMS.register("card_performance_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_REACTION_BASIC = ITEMS.register("card_reaction_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_POSE_BASIC = ITEMS.register("card_pose_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ITEMS.register(bus);
    }
}
