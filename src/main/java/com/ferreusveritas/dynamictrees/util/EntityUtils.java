package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class EntityUtils {

    /**
     * This is a copy of Entity.rayTrace which is client side only. There's no reason for this function to be
     * client-side only as all of it's calls are client/server compatible.
     *
     * @param entity             The {@link LivingEntity} to ray trace from.
     * @param blockReachDistance The {@code reachDistance} of the entity.
     * @param partialTick        The partial ticks.
     * @return The {@link BlockHitResult} created.
     */
    @Nullable
    public static BlockHitResult playerRayTrace(LivingEntity entity, double blockReachDistance, float partialTick) {
        Vec3 vec3d = entity.getEyePosition(partialTick);
        Vec3 vec3d1 = entity.getViewVector(partialTick);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return entity.level().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
    }

}
