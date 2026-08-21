package com.dtteam.dynamictrees.entity.animation;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class AnimationConstants {
    public static final float TREE_GRAVITY = 0.03f;
    public static final float TREE_ELASTICITY = 0.25f;
    public static final ResourceKey<DamageType> TREE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, DynamicTrees.location("falling_tree"));

    public static DamageSource treeDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(TREE_DAMAGE_TYPE));
    }
}
