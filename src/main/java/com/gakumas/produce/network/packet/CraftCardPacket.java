package com.gakumas.produce.network.packet;

import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.card.CardCatalog;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRarity;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.item.CardMaterialItem;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * クライアント → サーバー：スキルカード作業台で1枚のカードを解放（習得）する要求。
 * サーバー側で「同レア度素材を所持」「Pレベル解放済み」「未習得」を検証し、
 * 満たせば素材を1つ消費してコレクションへ追加する。
 */
public class CraftCardPacket {

    private final ResourceLocation cardId;

    public CraftCardPacket(ResourceLocation cardId) {
        this.cardId = cardId;
    }

    public static void encode(CraftCardPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.cardId);
    }

    public static CraftCardPacket decode(FriendlyByteBuf buf) {
        return new CraftCardPacket(buf.readResourceLocation());
    }

    public static void handle(CraftCardPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CardDefinition def = CardRegistry.get(msg.cardId).orElse(null);
            if (def == null) return;
            CardRarity rarity = CardCatalog.rarityOf(msg.cardId);

            player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
                if (deck.hasOwnedCard(msg.cardId)) {
                    player.displayClientMessage(Component.literal("既に習得済みです").withStyle(ChatFormatting.GRAY), true);
                    return;
                }
                if (def.getRequiredPLevel() > 0 && deck.getPLevel() < def.getRequiredPLevel()) {
                    player.displayClientMessage(Component.literal("Pレベルが足りません（必要 Lv." + def.getRequiredPLevel() + "）")
                            .withStyle(ChatFormatting.RED), true);
                    return;
                }
                if (!consumeMaterial(player, rarity)) {
                    player.displayClientMessage(Component.literal(rarity.getLabel() + "の素材が足りません")
                            .withStyle(ChatFormatting.RED), true);
                    return;
                }
                deck.addOwnedCard(msg.cardId);
                SyncHelper.syncOwned(player, deck);
                player.displayClientMessage(Component.literal("『" + def.getDisplayName() + "』を習得しました！")
                        .withStyle(ChatFormatting.GREEN), true);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    /** 指定レア度の素材を1つ探して消費する。見つかって消費できたら true。 */
    private static boolean consumeMaterial(ServerPlayer player, CardRarity rarity) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof CardMaterialItem mat && mat.getRarity() == rarity) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
