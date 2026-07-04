package com.gakumas.produce.capability;

import com.gakumas.produce.GakumasProduceMod;
import com.gakumas.produce.network.SyncHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GakumasProduceMod.MOD_ID)
public class CapabilityEvents {

    public static final ResourceLocation DECK_CAP_ID = new ResourceLocation(GakumasProduceMod.MOD_ID, "deck_data");

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IDeckData.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(DECK_CAP_ID, new DeckDataProvider());
        }
    }

    /** プレイヤーがクローンされた際にデッキ・バフ状態を引き継ぐ（死亡時だけでなく、次元移動系のクローンでも保持する） */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(DeckCapability.DECK_DATA).ifPresent(oldData -> {
            CompoundTag tag = oldData.serializeNBT();
            event.getEntity().getCapability(DeckCapability.DECK_DATA).ifPresent(newData -> newData.deserializeNBT(tag));
        });
        event.getOriginal().invalidateCaps();
    }

    /** ログイン時：習得済みコレクションをクライアントへ同期する（デッキ編成画面の表示に必要） */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> SyncHelper.syncOwned(sp, deck));
        }
    }

    /** リスポーン時にもコレクションを再同期する */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> SyncHelper.syncOwned(sp, deck));
        }
    }

    /** 次元移動時にもコレクションを再同期する */
    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(DeckCapability.DECK_DATA).ifPresent(deck -> SyncHelper.syncOwned(sp, deck));
        }
    }
}
