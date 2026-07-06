package com.idolcraft.item;

import com.idolcraft.card.CardRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * レア度別のカード素材。スキルカード作業台で、この素材と同じレア度かつP解放済みの
 * 任意のスキルカードを1枚選んで習得する（消費される）ための材料。
 */
public class CardMaterialItem extends Item {

    private final CardRarity rarity;

    public CardMaterialItem(Properties properties, CardRarity rarity) {
        super(properties);
        this.rarity = rarity;
    }

    public CardRarity getRarity() {
        return rarity;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("レア度: " + rarity.getLabel())
                .withStyle(Style.EMPTY.withColor(rarity.getColor())));
        tooltip.add(Component.literal("スキルカード作業台で" + rarity.getLabel() + "レアの任意のカードと引き換える")
                .withStyle(Style.EMPTY.withColor(rarity.getColor())));
    }
}

