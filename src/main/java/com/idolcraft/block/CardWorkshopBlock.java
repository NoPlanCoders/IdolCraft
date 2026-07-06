package com.idolcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * スキルカード作業台。右クリックでカード解放GUI（{@link com.idolcraft.client.gui.CardWorkshopScreen}）を開く。
 * 画面表示はクライアント側だけで完結し、実際の解放処理はGUIからサーバーへパケット送信して行う。
 */
public class CardWorkshopBlock extends Block {

    public CardWorkshopBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> com.idolcraft.client.ClientScreenOpener::openCardWorkshop);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

