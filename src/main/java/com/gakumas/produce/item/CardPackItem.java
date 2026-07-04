package com.gakumas.produce.item;

import com.gakumas.produce.card.CardCatalog;
import com.gakumas.produce.card.CardRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * レア度別カードパック。右クリックで開封すると、そのレア度のスキルカードが1枚ランダムで手に入る
 * （Iron's Spells 'n Spellbooks のスクロール入手のような、レア度素材からクラフト→開封の流れ）。
 * 得られるのはカードアイテムなので、さらに右クリックで習得してコレクションに加える。
 */
public class CardPackItem extends Item {

    private final CardRarity rarity;

    public CardPackItem(Properties properties, CardRarity rarity) {
        super(properties);
        this.rarity = rarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.success(stack);
        }
        ResourceLocation cardId = CardCatalog.randomOfRarity(rarity, sp.getRandom());
        if (cardId == null) return InteractionResultHolder.pass(stack);

        Item cardItem = ForgeRegistries.ITEMS.getValue(cardId);
        if (cardItem == null) return InteractionResultHolder.pass(stack);

        stack.shrink(1);
        ItemStack reward = new ItemStack(cardItem);
        if (!sp.getInventory().add(reward)) {
            sp.drop(reward, false);
        }
        String cardName = cardItem.getDescription().getString();
        sp.displayClientMessage(
                Component.literal(rarity.getLabel() + "パックから ")
                        .withStyle(Style.EMPTY.withColor(rarity.getColor()))
                        .append(Component.literal("『" + cardName + "』").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" を入手！").withStyle(Style.EMPTY.withColor(rarity.getColor()))),
                false);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("右クリックで開封：" + rarity.getLabel() + "レアのカードを1枚入手")
                .withStyle(Style.EMPTY.withColor(rarity.getColor())));
    }
}
