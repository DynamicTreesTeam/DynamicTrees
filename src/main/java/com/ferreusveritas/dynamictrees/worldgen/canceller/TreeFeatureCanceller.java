package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.*;

/**
 * This is default implementation of {@link ITreeFeatureCanceller}, cancelling any features which
 * have a config that extends the given class, or that extends {@link MultipleRandomFeatureConfig} and
 * contains a feature that has a config extending the given class. <br>
 *
 * @author Harley O'Connor
 */
public class TreeFeatureCanceller<T extends IFeatureConfig> implements ITreeFeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(Class<T> treeFeatureConfigClass) {
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

        final IFeatureConfig featureConfig = ((DecoratedFeatureConfig) configuredFeature.config).feature.get().config;

            /*  The following code removes vanilla trees from the biome's generator.
                There may be some problems as MultipleRandomFeatures can store other features too,
                so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
                but one (unclean) solution may be to add the non-tree features back to the generator. */

        if (featureConfig instanceof MultipleRandomFeatureConfig) {
            // Removes configuredFeature if it contains trees.
            return this.doesContainTrees((MultipleRandomFeatureConfig) featureConfig, biomeResLoc, treeCanceller);
        } else if (featureConfig instanceof DecoratedFeatureConfig) {
            final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) featureConfig).feature.get();
            final IFeatureConfig nextFeatureConfig = nextConfiguredFeature.config;
            final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();

            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && featureRegistryName != null &&
                    treeCanceller.shouldCancelFeature(biomeResLoc, featureRegistryName)) {
                return true; // Removes any individual trees.
            } else if (nextFeatureConfig instanceof MultipleRandomFeatureConfig) {
                // Removes configuredFeature if it contains trees.
                return this.doesContainTrees((MultipleRandomFeatureConfig) nextFeatureConfig, biomeResLoc, treeCanceller);
            }
        }

        return false;
    }

    /**
     * Checks if the given {@link MultipleRandomFeatureConfig} contains trees.
     *
     * @param featureConfig The MultipleRandomFeatureConfig to check.
     * @return True if trees were found.
     */
    private boolean doesContainTrees (MultipleRandomFeatureConfig featureConfig, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        for (ConfiguredRandomFeatureList feature : featureConfig.features) {
            final ConfiguredFeature<?, ?> currentConfiguredFeature = feature.feature.get();
            final ResourceLocation featureRegistryName = currentConfiguredFeature.feature.getRegistryName();

            if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.config) && featureRegistryName != null &&
                    treeCanceller.shouldCancelFeature(biomeResLoc, featureRegistryName))
                return true;
        }
        return false;
    }

}
