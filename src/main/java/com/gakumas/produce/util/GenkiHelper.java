package com.gakumas.produce.util;

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
     * コストを消費する。まず元気(Absorption)から減らし、足りない分は体力から直接減らす。
     * @return 実際に消費できたかどうか（体力が0以下になる場合でも強制的に消費し、あとの処理側でダウン扱いにするかは呼び出し側次第）
     */
    public static void consumeCost(ServerPlayer player, float cost) {
        if (cost <= 0) return;
        float genki = player.getAbsorptionAmount();
        if (genki >= cost) {
            player.setAbsorptionAmount(genki - cost);
            return;
        }
        float remainder = cost - genki;
        player.setAbsorptionAmount(0f);
        float newHealth = Math.max(0f, player.getHealth() - remainder);
        player.setHealth(newHealth);
    }

    /** コストを支払えるか（元気+体力の合計が足りているか。体力は最低1残す仕様にはしていないので0まで許容） */
    public static boolean canAfford(ServerPlayer player, float cost) {
        return (player.getAbsorptionAmount() + player.getHealth()) >= cost;
    }
}
