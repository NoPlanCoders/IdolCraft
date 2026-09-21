package com.idolcraft.registry;

import com.idolcraft.IdolCraft;
import com.idolcraft.card.CardRarity;
import com.idolcraft.item.CardMaterialItem;
import com.idolcraft.item.HandbookItem;
import com.idolcraft.item.ProduceCardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IdolCraft.MOD_ID);

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
            () -> new BlockItem(com.idolcraft.registry.ModBlocks.CARD_WORKSHOP.get(), new Item.Properties()));

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

    // ---- トラブルカード ----
    public static final RegistryObject<Item> CARD_DROWSINESS = ITEMS.register("card_drowsiness",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    // ---- フリープラン（本家wiki準拠） ----
    public static final RegistryObject<Item> CARD_FREE_FIRED_UP = ITEMS.register("card_free_fired_up",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_FIRST_STEP = ITEMS.register("card_free_first_step",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_BRIGHT_FUTURE = ITEMS.register("card_free_bright_future",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_IDOL_DECLARATION = ITEMS.register("card_free_idol_declaration",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_HIGH_TENSION = ITEMS.register("card_free_high_tension",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_TV_APPEARANCE = ITEMS.register("card_free_tv_appearance",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_DREAM_TO_FULFILL = ITEMS.register("card_free_dream_to_fulfill",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_IDOL_SPIRIT = ITEMS.register("card_free_idol_spirit",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_FREE_FRESH_START = ITEMS.register("card_free_fresh_start",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    // ---- ロジックプラン（本家wiki準拠） ----
    public static final RegistryObject<Item> CARD_LOGIC_GOOD_MORNING = ITEMS.register("card_logic_good_morning",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_EASY_CHAT = ITEMS.register("card_logic_easy_chat",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_JUST_A_BIT_MORE = ITEMS.register("card_logic_just_a_bit_more",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_CLAPPING = ITEMS.register("card_logic_clapping",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_ENERGETIC_GREETING = ITEMS.register("card_logic_energetic_greeting",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_LUCKY_CHARM = ITEMS.register("card_logic_lucky_charm",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_RECKLESS = ITEMS.register("card_logic_reckless",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_DAYDREAMING = ITEMS.register("card_logic_daydreaming",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_RESTART = ITEMS.register("card_logic_restart",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_EI_EI_OH = ITEMS.register("card_logic_ei_ei_oh",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_RHYTHMICAL = ITEMS.register("card_logic_rhythmical",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_FOND_MEMORY_LAUGH = ITEMS.register("card_logic_fond_memory_laugh",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_PASTEL_MOOD = ITEMS.register("card_logic_pastel_mood",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_ENCOURAGEMENT = ITEMS.register("card_logic_encouragement",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_HAPPY_CHARM = ITEMS.register("card_logic_happy_charm",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_MAKEOVER = ITEMS.register("card_logic_makeover",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_LOVELY_WINK = ITEMS.register("card_logic_lovely_wink",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_WORDS_OF_THANKS = ITEMS.register("card_logic_words_of_thanks",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_HEART_SIGNAL = ITEMS.register("card_logic_heart_signal",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_SPARKLE = ITEMS.register("card_logic_sparkle",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_EVERYONE_LOVES = ITEMS.register("card_logic_everyone_loves",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_GLITTER_CONFETTI = ITEMS.register("card_logic_glitter_confetti",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_OVERFLOWING_MEMORIES = ITEMS.register("card_logic_overflowing_memories",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_BONDING = ITEMS.register("card_logic_bonding",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_HAPPY_TIME = ITEMS.register("card_logic_happy_time",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_FANCY_CHARM = ITEMS.register("card_logic_fancy_charm",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_CANT_STOP_EXCITEMENT = ITEMS.register("card_logic_cant_stop_excitement",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_NIGHT_BEFORE = ITEMS.register("card_logic_night_before",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_SUNBATHING = ITEMS.register("card_logic_sunbathing",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_IMAGE_TRAINING = ITEMS.register("card_logic_image_training",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_FULL_OF_MOTIVATION = ITEMS.register("card_logic_full_of_motivation",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_DREAMY_MOOD = ITEMS.register("card_logic_dreamy_mood",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_UNSTOPPABLE_FEELINGS = ITEMS.register("card_logic_unstoppable_feelings",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_MAIDEN_HEART = ITEMS.register("card_logic_maiden_heart",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_ADVENTUROUS_SPIRIT = ITEMS.register("card_logic_adventurous_spirit",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_CAPRICIOUS_HEART = ITEMS.register("card_logic_capricious_heart",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_GROWING_PAINS = ITEMS.register("card_logic_growing_pains",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_200_SMILE = ITEMS.register("card_logic_200_smile",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_BLOOMING = ITEMS.register("card_logic_blooming",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_REACH_YOU = ITEMS.register("card_logic_reach_you",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_SHINE_FOR_YOU = ITEMS.register("card_logic_shine_for_you",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_THAT_PROMISE = ITEMS.register("card_logic_that_promise",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_MIRACLE_MAGIC = ITEMS.register("card_logic_miracle_magic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_GROWTH_SPURT_MAGIC = ITEMS.register("card_logic_growth_spurt_magic",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_PERFECT_POSE_FACE = ITEMS.register("card_logic_perfect_pose_face",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_IM_THE_STAR = ITEMS.register("card_logic_im_the_star",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_STARDUST_SENSATION = ITEMS.register("card_logic_stardust_sensation",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_NOTEBOOK_RESOLVE = ITEMS.register("card_logic_notebook_resolve",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_HANDWRITTEN_MESSAGE = ITEMS.register("card_logic_handwritten_message",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_HEART_FLUTTER = ITEMS.register("card_logic_heart_flutter",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_RAINBOW_DREAMER = ITEMS.register("card_logic_rainbow_dreamer",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_LOGIC_DREAMY_LIP = ITEMS.register("card_logic_dreamy_lip",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    // ---- アノマリープラン（本家wiki準拠） ----
    public static final RegistryObject<Item> CARD_ANOMALY_JUST_APPEAL = ITEMS.register("card_anomaly_just_appeal",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_STARLIGHT = ITEMS.register("card_anomaly_starlight",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ONE_STEP = ITEMS.register("card_anomaly_one_step",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_LUCKY = ITEMS.register("card_anomaly_lucky",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_STEADY_STACK = ITEMS.register("card_anomaly_steady_stack",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ALL_OUT = ITEMS.register("card_anomaly_all_out",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TURNAROUND = ITEMS.register("card_anomaly_turnaround",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_NONSTOP = ITEMS.register("card_anomaly_nonstop",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_HUSTLE = ITEMS.register("card_anomaly_hustle",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_HAPPY_NOTE = ITEMS.register("card_anomaly_happy_note",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_HAPPY_ACCIDENT = ITEMS.register("card_anomaly_happy_accident",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TEARFUL_MEMORY = ITEMS.register("card_anomaly_tearful_memory",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SETTING = ITEMS.register("card_anomaly_setting",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_COMEBACK = ITEMS.register("card_anomaly_comeback",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_READY_SET = ITEMS.register("card_anomaly_ready_set",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ACCELERANDO = ITEMS.register("card_anomaly_accelerando",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_BURSTING_PASSION = ITEMS.register("card_anomaly_bursting_passion",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SWEAT_AND_GROWTH = ITEMS.register("card_anomaly_sweat_and_growth",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_FIRST_IMPRESSION = ITEMS.register("card_anomaly_first_impression",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_OPENING_ACT = ITEMS.register("card_anomaly_opening_act",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_STARTING_SMILE = ITEMS.register("card_anomaly_starting_smile",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TREND_LEADER = ITEMS.register("card_anomaly_trend_leader",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_IDEAL_TEMPO = ITEMS.register("card_anomaly_ideal_tempo",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TRAINING_RESULT = ITEMS.register("card_anomaly_training_result",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_OVERDRIVE = ITEMS.register("card_anomaly_overdrive",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ANDANTE = ITEMS.register("card_anomaly_andante",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_POTENTIAL = ITEMS.register("card_anomaly_potential",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_COUNTDOWN = ITEMS.register("card_anomaly_countdown",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_MOTIVATION_BOOST = ITEMS.register("card_anomaly_motivation_boost",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_PRIDE = ITEMS.register("card_anomaly_pride",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_HYPE_MASTER = ITEMS.register("card_anomaly_hype_master",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_INFLUENCER = ITEMS.register("card_anomaly_influencer",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_PATIENCE = ITEMS.register("card_anomaly_patience",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_MUTUAL_IMPROVEMENT = ITEMS.register("card_anomaly_mutual_improvement",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_FULL_THROTTLE = ITEMS.register("card_anomaly_full_throttle",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_RISKY_CHANCE = ITEMS.register("card_anomaly_risky_chance",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TOUGHNESS = ITEMS.register("card_anomaly_toughness",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SENSE_OF_ACHIEVEMENT = ITEMS.register("card_anomaly_sense_of_achievement",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TAKE_FLIGHT = ITEMS.register("card_anomaly_take_flight",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TOTAL_ART = ITEMS.register("card_anomaly_total_art",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_MIND_BODY_SPIRIT = ITEMS.register("card_anomaly_mind_body_spirit",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SHINE = ITEMS.register("card_anomaly_shine",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_CLIMAX = ITEMS.register("card_anomaly_climax",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ALL_IN = ITEMS.register("card_anomaly_all_in",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_ENTERTAINER = ITEMS.register("card_anomaly_entertainer",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SOAR = ITEMS.register("card_anomaly_soar",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_BECOMING_IDOL = ITEMS.register("card_anomaly_becoming_idol",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_SINGLE_MINDED = ITEMS.register("card_anomaly_single_minded",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TO_THE_TOP = ITEMS.register("card_anomaly_to_the_top",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_RESOLVE = ITEMS.register("card_anomaly_resolve",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CARD_ANOMALY_TRUE_POTENTIAL = ITEMS.register("card_anomaly_true_potential",
            () -> new ProduceCardItem(new Item.Properties().stacksTo(1)));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ITEMS.register(bus);
    }
}

