package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
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
public class FungusFeatureCanceller<T extends IFeatureConfig> extends FeatureCanceller {

    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(final ResourceLocation registryName, final Class<T> fungusFeatureConfigClass) {
        super(registryName);
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

        final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) configuredFeature.config).feature.get();
        final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();

        return this.fungusFeatureConfigClass.isInstance(nextConfiguredFeature.config) && featureRegistryName != null &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
    }

}
