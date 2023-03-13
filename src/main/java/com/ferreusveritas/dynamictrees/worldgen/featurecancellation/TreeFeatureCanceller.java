package com.ferreusveritas.dynamictrees.worldgen.featurecancellation;

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

import java.util.Set;


public class TreeFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(final ResourceLocation registryName, Class<T> treeFeatureConfigClass) {
        super(registryName);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        /*  The following code removes vanilla trees from the biome's generator.
            There may be some problems as MultipleRandomFeatures can store other features too,
            so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
            but one (unclean) solution may be to add the non-tree features back to the generator. */

        if (featureConfig instanceof RandomFeatureConfiguration) {
            // Removes configuredFeature if it contains trees.
            return this.doesContainTrees((RandomFeatureConfiguration) featureConfig, namespaces);
        } else if (treeFeatureConfigClass.isInstance(featureConfig)) {
            String namespace = "";
            final ConfiguredFeature<?, ?> nextConfiguredFeature = configuredFeature.getFeatures().findFirst().get();
            final FeatureConfiguration nextFeatureConfig = nextConfiguredFeature.config();
            final ResourceLocation featureRegistryName = nextConfiguredFeature.feature().getRegistryName();
            if(featureRegistryName != null) {
                namespace = featureRegistryName.getNamespace();
            }
            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && !namespace.equals("") &&
                    namespaces.contains(namespace)) {
                return true; // Removes any individual trees.
            } else if (nextFeatureConfig instanceof RandomFeatureConfiguration) {
                // Removes configuredFeature if it contains trees.
                return this.doesContainTrees((RandomFeatureConfiguration) nextFeatureConfig, namespaces);
            }
        }

        return configuredFeature.getFeatures().filter(abc -> abc.feature() instanceof TreeFeature).count() > 0;
    }


    private boolean doesContainTrees(RandomFeatureConfiguration featureConfig, Set<String> namespaces) {
        for (WeightedPlacedFeature feature : featureConfig.features) {
            final PlacedFeature currentConfiguredFeature = feature.feature.value();
            final ResourceLocation featureRegistryName = currentConfiguredFeature.getFeatures().findFirst().get().feature().getRegistryName();

            if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.placement()) && featureRegistryName != null &&
                    namespaces.contains(featureRegistryName.getNamespace())) {
                return true;
            }
        }
        return false;
    }

}
