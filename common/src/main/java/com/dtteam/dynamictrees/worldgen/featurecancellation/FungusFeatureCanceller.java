package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling fungus features.
 * It cancels any features that have a config that extends the given class.
 *
 * @param <T> An {@link FeatureConfiguration} which should be cancelled.
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(final Identifier registryName, final Class<T> fungusFeatureConfigClass) {
        super(registryName);
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configuredFeature.feature());

        return featureRegistryName != null && this.fungusFeatureConfigClass.isInstance(configuredFeature.config()) &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
    }
}