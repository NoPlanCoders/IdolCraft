package com.gakumas.produce.item;

import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRarity;
import com.gakumas.produce.card.CardCatalog;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.card.CardType;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import java.util.Optional;

/**
 * すべてのカードに共通する汎用アイテムクラス。
 * カードごとの個別ロジックは持たず、自身の registry name をキーに
 * {@link CardRegistry} から {@link CardDefinition} を引いて挙動を決定する。
 * これにより新カード追加時は Item の実装を増やさず、CardDefinition の登録だけで完結する。
 */
public class ProduceCardItem extends Item {

    public ProduceCardItem(Properties properties) {
        super(properties);
    }

    public Optional<CardDefinition> getDefinition() {
        var key = ForgeRegistries.ITEMS.getKey(this);
        if (key == null) return Optional.empty();
        return CardRegistry.get(key);
    }

    /**
     * 右クリックでカードを「習得」する。習得済みコレクションに恒久追加され、
     * 以後デッキ編成で使えるようになる。習得したカード1枚を消費する。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.success(stack);
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(this);
        if (id == null) return InteractionResultHolder.pass(stack);
        String name = getDefinition().map(CardDefinition::getDisplayName).orElse(id.getPath());

        sp.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            if (deck.addOwnedCard(id)) {
                stack.shrink(1);
                sp.displayClientMessage(
                        Component.literal("『" + name + "』をコレクションに習得しました！").withStyle(ChatFormatting.GREEN), true);
                SyncHelper.syncOwned(sp, deck);
            } else {
                sp.displayClientMessage(
                        Component.literal("『" + name + "』は習得済みです").withStyle(ChatFormatting.GRAY), true);
            }
        });
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        getDefinition().ifPresent(def -> {
            CardRarity rarity = CardCatalog.rarityOf(def.getId());
            tooltip.add(Component.literal("レア度: " + rarity.getLabel())
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(rarity.getColor())));
            tooltip.add(Component.literal("右クリックで習得").withStyle(ChatFormatting.DARK_GRAY));
            // 本家同様、通常カードには特にタグを付けず、「レッスン中1回」カードのみ明示する
            if (def.getType() == CardType.ONCE_PER_LESSON) {
                tooltip.add(Component.literal("レッスン中1回").withStyle(ChatFormatting.GOLD));
            }
            if (def.getHpCost() > 0) {
                tooltip.add(Component.literal("消費体力: " + def.getHpCost()).withStyle(ChatFormatting.RED));
            }
            if (def.getRequiredAdvancement() != null) {
                tooltip.add(Component.literal("必要プロデューサーランクあり").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            if (def.getRequiredPLevel() > 0) {
                tooltip.add(Component.literal("必要Pレベル: " + def.getRequiredPLevel()).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            // 使用条件を満たさない場合は手札上で減光表示されるため、ここではテキストで重ねて説明しない
        });
    }
}
