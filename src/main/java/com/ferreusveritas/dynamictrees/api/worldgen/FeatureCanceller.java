package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * @author Harley O'Connor
 */
public abstract class FeatureCanceller extends RegistryEntry<FeatureCanceller> {

    public static final FeatureCanceller NULL_CANCELLER = new FeatureCanceller(DTTrees.NULL) {
        @Override
        public boolean shouldCancel(PlacedFeature configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
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
    public abstract boolean shouldCancel(PlacedFeature configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations);

}
