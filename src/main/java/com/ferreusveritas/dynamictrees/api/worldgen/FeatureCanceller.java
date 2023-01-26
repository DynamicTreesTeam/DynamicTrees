package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Set;

/**
 * @author Harley O'Connor
 */
public abstract class FeatureCanceller extends RegistryEntry<FeatureCanceller> {

    public static final FeatureCanceller NULL_CANCELLER = new FeatureCanceller(DTTrees.NULL) {
        @Override
        public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces) {
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
     * @param configuredFeature the feature to check
     * @param namespaces        the namespaces of features that should be cancelled
     * @return True if feature should be cancelled, false if not.
     */
    public abstract boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces);

}
