package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.ForgeRegistries;


public class TreeFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(final ResourceLocation registryName, Class<T> treeFeatureConfigClass) {
        super(registryName);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        /*  The following code removes vanilla trees from the biome's generator.
            There may be some problems as MultipleRandomFeatures can store other features too,
            so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
            but one (unclean) solution may be to add the non-tree features back to the generator. */

        if (featureConfig instanceof RandomFeatureConfiguration) {
            // Removes configuredFeature if it contains trees.
            return this.doesContainTrees((RandomFeatureConfiguration) featureConfig, featureCancellations);
        } else if (featureConfig instanceof TreeConfiguration) {
            String nameSpace = "";
            final ConfiguredFeature<?, ?> nextConfiguredFeature = configuredFeature.getFeatures().findFirst().get();
            final FeatureConfiguration nextFeatureConfig = nextConfiguredFeature.config();
            final ResourceLocation featureRegistryName = ForgeRegistries.FEATURES.getKey(nextConfiguredFeature.feature());
            if(featureRegistryName != null) {
                nameSpace = featureRegistryName.getNamespace();
            }
            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && !nameSpace.equals("") &&
                    featureCancellations.shouldCancelNamespace(nameSpace)) {
                return true; // Removes any individual trees.
            } else if (nextFeatureConfig instanceof RandomFeatureConfiguration) {
                // Removes configuredFeature if it contains trees.
                return this.doesContainTrees((RandomFeatureConfiguration) nextFeatureConfig, featureCancellations);
            }
        }
        if (configuredFeature == DTRegistries.DYNAMIC_TREE_CONFIGURED_FEATURE.get()) {
            return false;
        }

        return configuredFeature.getFeatures().filter(abc -> abc.feature() instanceof TreeFeature).count() > 0;
    }


    private boolean doesContainTrees(RandomFeatureConfiguration featureConfig, BiomePropertySelectors.FeatureCancellations featureCancellations) {
        for (WeightedPlacedFeature feature : featureConfig.features) {
            final PlacedFeature currentConfiguredFeature = feature.feature.value();
            final ResourceLocation featureRegistryName = ForgeRegistries.FEATURES.getKey(currentConfiguredFeature.getFeatures().findFirst().get().feature());

            if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.placement()) && featureRegistryName != null &&
                    featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
                return true;
            }
        }
        return false;
    }

}
