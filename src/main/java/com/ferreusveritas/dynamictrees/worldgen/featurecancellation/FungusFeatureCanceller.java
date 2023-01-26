package com.ferreusveritas.dynamictrees.worldgen.featurecancellation;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Set;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling fungus features.
 * It cancels any features that have a config that extends the given class.
 *
 * @param <T> An {@link FeatureConfiguration} which should be cancelled.
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(final ResourceLocation registryName, final Class<T> fungusFeatureConfigClass) {
        super(registryName);
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces) {
       final ResourceLocation featureName = configuredFeature.feature().getRegistryName();

       return featureName != null && this.fungusFeatureConfigClass.isInstance(configuredFeature.config()) &&
               namespaces.contains(featureName.getNamespace());
    }
}
