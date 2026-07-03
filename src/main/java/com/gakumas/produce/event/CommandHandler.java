package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.capability.DeckService;
import com.gakumas.produce.item.HandbookItem;
import com.gakumas.produce.network.SyncHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.brigadier.builder.ArgumentBuilder;

@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommandHandler {

    private CommandHandler() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("gakumas")
            .then(registerDeckCommands())
            .then(registerBuffCommands())
            .then(registerRankCommands())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerRankCommands() {
        return Commands.literal("rank")
            .then(Commands.literal("set")
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 60))
                    .executes(context -> runSetRank(context, IntegerArgumentType.getInteger(context, "level")))))
            .then(Commands.literal("addxp")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> runAddRankXp(context, IntegerArgumentType.getInteger(context, "amount")))));
    }

    private static int runSetRank(CommandContext<CommandSourceStack> context, int level) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.setPLevel(level);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal("プロデューサーランクを Lv." + level + " に設定しました。"), true);
        return 1;
    }

    private static int runAddRankXp(CommandContext<CommandSourceStack> context, int amount) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.addProduceXp(amount);
            SyncHelper.syncTo(player, deck);
            context.getSource().sendSuccess(
                    () -> Component.literal("プロデュース経験値 +" + amount + "（現在 Lv." + deck.getPLevel() + "）"), true);
        });
        return 1;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerDeckCommands() {
        return Commands.literal("deck")
            .then(Commands.literal("reset").executes(CommandHandler::runDeckReset))
            .then(Commands.literal("use").executes(context -> runDeckAction(context, false)))
            .then(Commands.literal("skip").executes(context -> runDeckAction(context, true)))
            .then(Commands.literal("select")
                .then(Commands.argument("delta", IntegerArgumentType.integer(-64, 64))
                    .executes(context -> runSelect(context, IntegerArgumentType.getInteger(context, "delta")))));
        }

    private static ArgumentBuilder<CommandSourceStack, ?> registerBuffCommands() {
        return Commands.literal("buff")
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.literal("focus")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> runAddFocus(context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("good")
                    .then(Commands.argument("turns", IntegerArgumentType.integer(1))
                        .executes(context -> runAddGoodCondition(context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "turns")))))
                .then(Commands.literal("great")
                    .then(Commands.argument("turns", IntegerArgumentType.integer(1))
                        .executes(context -> runAddGreatCondition(context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "turns")))))
                .then(Commands.literal("clear")
                    .executes(context -> runClearBuffs(context, EntityArgument.getPlayer(context, "target")))));
        }

    private static int runDeckReset(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.resetDeck(player, deck);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal("デッキをリセットしました。"), false);
        return 1;
    }

    private static int runDeckAction(CommandContext<CommandSourceStack> context, boolean skip) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        if (!isHoldingHandbook(player)) {
            context.getSource().sendFailure(Component.literal("手帳を持っているときだけ実行できます。"));
            return 0;
        }

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.performAction(player, deck, skip);
            SyncHelper.syncTo(player, deck);
        });

        context.getSource().sendSuccess(
                () -> Component.literal(skip ? "カードをスキップしました。" : "カードを使用しました。"),
                false
        );
        return 1;
    }

    private static int runSelect(CommandContext<CommandSourceStack> context, int delta) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        if (!isHoldingHandbook(player)) {
            context.getSource().sendFailure(Component.literal("手帳を持っているときだけ実行できます。"));
            return 0;
        }

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            DeckService.changeSelection(deck, delta);
            SyncHelper.syncTo(player, deck);
        });

        context.getSource().sendSuccess(() -> Component.literal("手札の選択位置を変更しました。"), false);
        return 1;
    }

    private static int runAddFocus(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().addFocus(amount);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " に集中 +" + amount + " を付与しました。"), true);
        return 1;
    }

    private static int runAddGoodCondition(CommandContext<CommandSourceStack> context, ServerPlayer player, int turns) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().addGoodCondition(turns * DeckService.TICKS_PER_TURN, turns);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " に好調 " + turns + "ターン を付与しました。"), true);
        return 1;
    }

    private static int runAddGreatCondition(CommandContext<CommandSourceStack> context, ServerPlayer player, int turns) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().addGreatCondition(turns * DeckService.TICKS_PER_TURN);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " に絶好調 " + turns + "ターン を付与しました。"), true);
        return 1;
    }

    private static int runClearBuffs(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().clearStatusBuffs();
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " の集中・好調・絶好調を消しました。"), true);
        return 1;
    }

    private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer)) {
            context.getSource().sendFailure(Component.literal("このコマンドはプレイヤーとして実行してください。"));
            return null;
        }
        return (ServerPlayer) context.getSource().getEntity();
    }

    private static boolean isHoldingHandbook(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof HandbookItem
                || player.getOffhandItem().getItem() instanceof HandbookItem;
    }
}