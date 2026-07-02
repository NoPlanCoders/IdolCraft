package com.gakumas.produce.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class TargetingHelper {

    private static final double RANGE = 12.0;

    private TargetingHelper() {}

    /** プレイヤーの視線上にある最も近い LivingEntity を取得する（カード「アピールの基本」等のターゲット取得用） */
    @Nullable
    public static LivingEntity getLookTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * RANGE, look.y * RANGE, look.z * RANGE);
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D);

        EntityHitResult result = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(), player, eye, end, searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && e != player
        );
        if (result != null && result.getType() == HitResult.Type.ENTITY) {
            Entity e = result.getEntity();
            if (e instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }
}
