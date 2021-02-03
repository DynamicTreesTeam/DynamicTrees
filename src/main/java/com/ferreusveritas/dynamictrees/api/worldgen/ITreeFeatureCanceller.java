package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.ConfiguredFeature;

/**
 * @author Harley O'Connor
 */
public interface ITreeFeatureCanceller {

    /**
     * Works out if the configured feature in the given biome should be cancelled or not.
     *
     * @param configuredFeature The configured feature.
     * @param biomeResLoc The biome's registry name.
     * @param treeCanceller The tree canceller object.
     * @return True if feature should be cancelled, false if not.
     */
    boolean shouldCancel (ConfiguredFeature<?, ?> configuredFeature, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller);

}
