package com.gakumas.produce.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 「プロデュース手帳」。これを所持している間、プレイヤーにデッキUIが表示され、
 * 右クリックでカード発動、スニーク+スクロールでカード選択ができるようになる。
 */
public class HandbookItem extends Item {
    public HandbookItem(Properties properties) {
        super(properties);
    }

    /** 魔導書的な特別感を出すため、常にエンチャント風の輝き（フォイル）を纏わせる */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
