package com.idolcraft.event;

import com.idolcraft.IdolCraft;
import com.idolcraft.capability.DeckCapability;
import com.idolcraft.capability.DeckService;
import com.idolcraft.item.HandbookItem;
import com.idolcraft.network.SyncHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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

@Mod.EventBusSubscriber(modid = IdolCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommandHandler {

    private CommandHandler() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("gakumas")
            .then(registerDeckCommands())
            .then(registerBuffCommands())
            .then(registerPLevelCommands())
            .then(registerCardCommands())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> registerCardCommands() {
        return Commands.literal("cards")
            .then(Commands.literal("unlockall").executes(CommandHandler::runUnlockAll))
            .then(Commands.literal("clear").executes(CommandHandler::runClearCards));
    }

    private static int runUnlockAll(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            int[] added = {0};
            for (com.idolcraft.card.CardDefinition def : com.idolcraft.card.CardRegistry.all()) {
                if (deck.addOwnedCard(def.getId())) added[0]++;
            }
            SyncHelper.syncOwned(player, deck);
            context.getSource().sendSuccess(() -> Component.literal("全カードを習得しました（+" + added[0] + "枚）。"), true);
        });
        return 1;
    }

    private static int runClearCards(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getPlayer(context);
        if (player == null) return 0;
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getOwnedCards().clear();
            SyncHelper.syncOwned(player, deck);
            context.getSource().sendSuccess(() -> Component.literal("習得済みカードのコレクションを空にしました。"), true);
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
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(context -> runAddFocus(context,
                            EntityArgument.getPlayer(context, "target"),
                            LongArgumentType.getLong(context, "amount")))))
                .then(Commands.literal("good")
                    .then(Commands.argument("turns", LongArgumentType.longArg(1))
                        .executes(context -> runAddGoodCondition(context,
                            EntityArgument.getPlayer(context, "target"),
                            LongArgumentType.getLong(context, "turns")))))
                .then(Commands.literal("great")
                    .then(Commands.argument("turns", LongArgumentType.longArg(1))
                        .executes(context -> runAddGreatCondition(context,
                            EntityArgument.getPlayer(context, "target"),
                            LongArgumentType.getLong(context, "turns")))))
                .then(Commands.literal("clear")
                    .executes(context -> runClearBuffs(context, EntityArgument.getPlayer(context, "target")))));
        }

    private static ArgumentBuilder<CommandSourceStack, ?> registerPLevelCommands() {
        return Commands.literal("plevel")
            .then(Commands.literal("set")
                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                    .executes(context -> runSetPLevel(
                        context,
                        getPlayer(context),
                        IntegerArgumentType.getInteger(context, "level")
                    ))))
            .then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(-1000, 1000))
                    .executes(context -> runAddPLevel(
                        context,
                        getPlayer(context),
                        IntegerArgumentType.getInteger(context, "amount")
                    ))))
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.literal("set")
                    .then(Commands.argument("level", IntegerArgumentType.integer(1))
                        .executes(context -> runSetPLevel(
                            context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "level")
                        ))))
                .then(Commands.literal("add")
                    .then(Commands.argument("amount", IntegerArgumentType.integer(-1000, 1000))
                        .executes(context -> runAddPLevel(
                            context,
                            EntityArgument.getPlayer(context, "target"),
                            IntegerArgumentType.getInteger(context, "amount")
                        )))));
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

    private static int runAddFocus(CommandContext<CommandSourceStack> context, ServerPlayer player, long amount) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().addFocus(amount);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " に集中 +" + amount + " を付与しました。"), true);
        return 1;
    }

    private static int runAddGoodCondition(CommandContext<CommandSourceStack> context, ServerPlayer player, long turns) {
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.getBuffState().addGoodCondition(turns * DeckService.TICKS_PER_TURN, turns);
            SyncHelper.syncTo(player, deck);
        });
        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " に好調 " + turns + "ターン を付与しました。"), true);
        return 1;
    }

    private static int runAddGreatCondition(CommandContext<CommandSourceStack> context, ServerPlayer player, long turns) {
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

    private static int runSetPLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int level) {
        final boolean[] updated = {false};
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.setPLevel(level);
            SyncHelper.syncTo(player, deck);
            context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " のPレベルを Lv." + deck.getPLevel() + " に設定しました。"), true);
            updated[0] = true;
        });
        if (!updated[0]) {
            context.getSource().sendFailure(Component.literal("Pレベルの更新に失敗しました。"));
        }
        return 1;
    }

    private static int runAddPLevel(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {
        final boolean[] updated = {false};
        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            deck.setPLevel(deck.getPLevel() + amount);
            SyncHelper.syncTo(player, deck);
            context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " のPレベルを Lv." + deck.getPLevel() + " にしました。"), true);
            updated[0] = true;
        });
        if (!updated[0]) {
            context.getSource().sendFailure(Component.literal("Pレベルの更新に失敗しました。"));
        }
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
