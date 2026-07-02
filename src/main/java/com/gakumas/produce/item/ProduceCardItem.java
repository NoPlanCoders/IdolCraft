package com.gakumas.produce.item;

import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.card.CardType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        getDefinition().ifPresent(def -> {
            tooltip.add(Component.literal(def.getType() == CardType.L_CARD ? "[Lカード]" : "[Oカード]")
                    .withStyle(def.getType() == CardType.L_CARD ? ChatFormatting.GRAY : ChatFormatting.GOLD));
            if (def.getHpCost() > 0) {
                tooltip.add(Component.literal("消費体力: " + def.getHpCost()).withStyle(ChatFormatting.RED));
            }
            if (def.getRequiredAdvancement() != null) {
                tooltip.add(Component.literal("必要プロデューサーランクあり").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        });
    }
}
