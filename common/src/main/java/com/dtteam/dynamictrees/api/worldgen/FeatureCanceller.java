package com.dtteam.dynamictrees.api.worldgen;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * @author Harley O'Connor
 */
public abstract class FeatureCanceller extends RegistryEntry<FeatureCanceller> {

    public static final FeatureCanceller NULL_CANCELLER = new FeatureCanceller(DynamicTrees.NULL) {
        @Override
        public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
            return false;
        }
    };

    public static final SimpleRegistry<FeatureCanceller> REGISTRY = new SimpleRegistry<>(FeatureCanceller.class, NULL_CANCELLER);

    public FeatureCanceller(final ResourceLocation registryName) {
        super(registryName);
    }

    /**
     * Works out if the configured feature in the given biome should be cancelled or not.
     *
     * @param configuredFeature    The configured feature.
     * @param featureCancellations The tree canceller object.
     * @return True if feature should be cancelled, false if not.
     */
    public abstract boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations);

}