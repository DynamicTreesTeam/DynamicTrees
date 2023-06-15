package com.ferreusveritas.dynamictrees.entity.animation;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class AnimationConstants {
    public static final float TREE_GRAVITY = 0.03f;
    public static final float TREE_ELASTICITY = 0.25f;
    public static final ResourceKey<DamageType> TREE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DynamicTrees.MOD_ID, "falling_tree"));

    public static DamageSource treeDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TREE_DAMAGE_TYPE));
    }
}
