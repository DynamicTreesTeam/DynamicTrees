package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling
 * fungus features. It cancels any features that have a config that extends the given class.
 *
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends IFeatureConfig> implements ITreeFeatureCanceller {

    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(Class<T> fungusFeatureConfigClass) {
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

        final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) configuredFeature.config).feature.get();
        return this.fungusFeatureConfigClass.isInstance(nextConfiguredFeature.config) &&
                treeCanceller.shouldCancelFeature(biomeResLoc, nextConfiguredFeature.feature.getRegistryName());
    }

}
