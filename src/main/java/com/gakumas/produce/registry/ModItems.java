package com.gakumas.produce.registry;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.card.CardRarity;
import com.gakumas.produce.item.CardMaterialItem;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.item.ProduceCardItem;
import net.minecraft.world.item.BlockItem;
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

    // ---- レア度別カード素材（スキルカード作業台で同レアの任意カードと引き換える） ----
    public static final RegistryObject<Item> MATERIAL_WHITE = ITEMS.register("card_material_white",
            () -> new CardMaterialItem(new Item.Properties(), CardRarity.WHITE));
    public static final RegistryObject<Item> MATERIAL_SILVER = ITEMS.register("card_material_silver",
            () -> new CardMaterialItem(new Item.Properties(), CardRarity.SILVER));
    public static final RegistryObject<Item> MATERIAL_GOLD = ITEMS.register("card_material_gold",
            () -> new CardMaterialItem(new Item.Properties(), CardRarity.GOLD));
    public static final RegistryObject<Item> MATERIAL_RAINBOW = ITEMS.register("card_material_rainbow",
            () -> new CardMaterialItem(new Item.Properties(), CardRarity.RAINBOW));

    // ---- スキルカード作業台ブロックのアイテム ----
    public static final RegistryObject<Item> CARD_WORKSHOP = ITEMS.register("card_workshop",
            () -> new BlockItem(com.gakumas.produce.registry.ModBlocks.CARD_WORKSHOP.get(), new Item.Properties()));

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


    // ---- センスプラン 追加カード（本家wiki準拠） ----
    public static final RegistryObject<Item> CARD_CHALLENGE = ITEMS.register("card_challenge",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_TRIAL_ERROR = ITEMS.register("card_trial_error",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_GAZE_BASIC = ITEMS.register("card_gaze_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_THINKING_BASIC = ITEMS.register("card_thinking_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_COMPOSURE_BASIC = ITEMS.register("card_composure_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_TIMING_BASIC = ITEMS.register("card_timing_basic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LIGHT_STEPS = ITEMS.register("card_light_steps",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_CHARM = ITEMS.register("card_charm",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_WARMUP = ITEMS.register("card_warmup",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FAN_SERVICE = ITEMS.register("card_fan_service",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_MOMENTUM = ITEMS.register("card_momentum",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_HIGH_TOUCH = ITEMS.register("card_high_touch",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_TALK_TIME = ITEMS.register("card_talk_time",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_COURSE_CORRECTION = ITEMS.register("card_course_correction",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_PUMP_UP = ITEMS.register("card_pump_up",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_PACING = ITEMS.register("card_pacing",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_BALANCE_SENSE = ITEMS.register("card_balance_sense",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_OPTIMISTIC = ITEMS.register("card_optimistic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_DEEP_BREATH = ITEMS.register("card_deep_breath",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ONE_BREATH = ITEMS.register("card_one_breath",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_DECIDED_POSE = ITEMS.register("card_decided_pose",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ADLIB = ITEMS.register("card_adlib",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_PASSION_TURN = ITEMS.register("card_passion_turn",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LEAP = ITEMS.register("card_leap",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_BLESSING = ITEMS.register("card_blessing",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_START_DASH = ITEMS.register("card_start_dash",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_STAND_PLAY = ITEMS.register("card_stand_play",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_POSITION_CHECK = ITEMS.register("card_position_check",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_UNSTOPPABLE = ITEMS.register("card_unstoppable",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_KEEN_EYE = ITEMS.register("card_keen_eye",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_BIG_CHEER = ITEMS.register("card_big_cheer",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_POWER_OF_WISH = ITEMS.register("card_power_of_wish",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_STARTING_SIGNAL = ITEMS.register("card_starting_signal",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_GRIT = ITEMS.register("card_grit",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_PATH_TO_SUCCESS = ITEMS.register("card_path_to_success",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_SPOTLIGHT = ITEMS.register("card_spotlight",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ONE_SHOT = ITEMS.register("card_one_shot",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_THRILLING = ITEMS.register("card_thrilling",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FEARLESS = ITEMS.register("card_fearless",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_MENTAL_UNITY = ITEMS.register("card_mental_unity",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_BUZZWORD = ITEMS.register("card_buzzword",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FULFILLMENT = ITEMS.register("card_fulfillment",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_CHARMING_PERFORMANCE = ITEMS.register("card_charming_performance",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_SUPREME_ENTERTAINMENT = ITEMS.register("card_supreme_entertainment",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_AWAKENING = ITEMS.register("card_awakening",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LIMELIGHT = ITEMS.register("card_limelight",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_HOT_TOPIC = ITEMS.register("card_hot_topic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_NATIONAL_IDOL = ITEMS.register("card_national_idol",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ENDLESS_APPLAUSE = ITEMS.register("card_endless_applause",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_NATURAL_TALENT = ITEMS.register("card_natural_talent",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ITEMS.register(bus);
    }
}
