package com.gakumas.produce.event;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.capability.DeckCapability;
import com.gakumas.produce.util.ScoreMath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
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
