package com.idolcraft.util;

import net.minecraft.server.level.ServerPlayer;

/**
 * 「元気」= バニラの衝撃吸収(Absorption)を流用したリソース。
 * コスト消費時は元気(Absorption)から優先的に減算し、余った分だけ体力(Health)を減らす。
 */
public final class GenkiHelper {

    private GenkiHelper() {}

    public static float getGenki(ServerPlayer player) {
        return player.getAbsorptionAmount();
    }

    public static void addGenki(ServerPlayer player, float amount) {
        if (amount <= 0) return;
        player.setAbsorptionAmount(player.getAbsorptionAmount() + amount);
    }

    /**
     * コストを消費する。バニラの{@link ServerPlayer#hurt}を使うことで、
     * 元気(Absorption)の優先消費・体力減少・0以下になった場合の正規の死亡処理
     * （LivingDeathEvent発火、アイテムドロップ等）をすべてMinecraft標準の仕組みに任せる。
     * 直接 setHealth() していた旧実装は死亡パイプラインを迂回してしまい、
     * コストで力尽きた際にアイテムがドロップしない不具合の原因になっていたため修正した。
     */
    public static void consumeCost(ServerPlayer player, float cost) {
        if (cost <= 0) return;
        // コストは「無敵時間」で無効化されるべきではないため、直前にリセットしてから確実にダメージを通す
        player.invulnerableTime = 0;
        player.hurt(player.level().damageSources().magic(), cost);
    }

    /** コストを支払えるか（元気+体力の合計が足りているか。体力は最低1残す仕様にはしていないので0まで許容） */
    public static boolean canAfford(ServerPlayer player, float cost) {
        return (player.getAbsorptionAmount() + player.getHealth()) >= cost;
    }
}

