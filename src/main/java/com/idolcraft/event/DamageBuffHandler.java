package com.idolcraft.event;

import com.idolcraft.IdolCraft;
import com.idolcraft.capability.DeckCapability;
import com.idolcraft.util.ScoreMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IdolCraft.MOD_ID)
public class DamageBuffHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> {
            if (deck.getBuffState().getFocusStacks() <= 0
                    && !deck.getBuffState().isGoodConditionActive()
                    && !deck.getBuffState().isGreatConditionActive()) {
                return;
            }

            event.setAmount(ScoreMath.calculateOutgoingDamage(event.getAmount(), deck.getBuffState()));
        });
    }
}

