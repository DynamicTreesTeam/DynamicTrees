package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling fungus features.
 * It cancels any features that have a config that extends the given class.
 *
 * @param <T> An {@link IFeatureConfig} which should be cancelled.
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(final ResourceLocation registryName, final Class<T> fungusFeatureConfigClass) {
        super(registryName);
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        if (!(configuredFeature.config instanceof DecoratedFeatureConfiguration)) {
            return false;
        }

        final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfiguration) configuredFeature.config).feature.get();
        final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();

        return this.fungusFeatureConfigClass.isInstance(nextConfiguredFeature.config) && featureRegistryName != null &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
    }

}
