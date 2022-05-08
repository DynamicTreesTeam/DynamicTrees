package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


public class TreeFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(final ResourceLocation registryName, Class<T> treeFeatureConfigClass) {
        super(registryName);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }
//todo: figure out
    @Override
    public boolean shouldCancel(PlacedFeature configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {


        final FeatureConfiguration featureConfig = ((FeatureConfiguration) configuredFeature.getFeatures().findFirst().get().config);//.feature.get().config;

        /*  The following code removes vanilla trees from the biome's generator.
            There may be some problems as MultipleRandomFeatures can store other features too,
            so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
            but one (unclean) solution may be to add the non-tree features back to the generator. */

//        if (featureConfig instanceof RandomFeatureConfiguration) {
//            // Removes configuredFeature if it contains trees.
//            return this.doesContainTrees((RandomFeatureConfiguration) featureConfig, featureCancellations);
//        } else if (featureConfig instanceof TreeConfiguration) {
//            final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfiguration) featureConfig).feature.get();
//            final FeatureConfiguration nextFeatureConfig = nextConfiguredFeature.config;
//            final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();
//
//            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && featureRegistryName != null &&
//                    featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
//                return true; // Removes any individual trees.
//            } else if (nextFeatureConfig instanceof RandomFeatureConfiguration) {
//                // Removes configuredFeature if it contains trees.
//                return this.doesContainTrees((RandomFeatureConfiguration) nextFeatureConfig, featureCancellations);
//            }
//        }

        return configuredFeature.getFeatures().filter(abc -> abc.feature instanceof TreeFeature).count() > 0;
    }


    private boolean doesContainTrees(RandomFeatureConfiguration featureConfig, BiomePropertySelectors.FeatureCancellations featureCancellations) {
//        for (WeightedPlacedFeature feature : featureConfig.features) {
//            final PlacedFeature currentConfiguredFeature = feature.feature.get();
//            final ResourceLocation featureRegistryName = currentConfiguredFeature.feature.getRegistryName();
//
//            if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.config) && featureRegistryName != null &&
//                    featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
//                return true;
//            }
//        }
//        return false;
        return true;
    }

}
