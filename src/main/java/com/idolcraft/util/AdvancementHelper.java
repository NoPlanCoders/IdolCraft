package com.idolcraft.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AdvancementHelper {

    private AdvancementHelper() {}

    /** 指定した進捗をプレイヤーが達成済みかどうか判定する。進捗IDがnullの場合は常にtrue（制限なし） */
    public static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        if (advancementId == null) return true;
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            // 進捗が未定義の場合は安全側に倒して「未達成」扱いにする
            return false;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }
}

