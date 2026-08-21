package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TreeFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> treeFeatureConfigClass;

    public TreeFeatureCanceller(final Identifier registryName, Class<T> treeFeatureConfigClass) {
        super(registryName);
        this.treeFeatureConfigClass = treeFeatureConfigClass;
    }

    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        if (featureConfig instanceof RandomFeatureConfiguration randomConfig) {
            return this.doesContainTrees(randomConfig, featureCancellations);
        } else if (this.treeFeatureConfigClass.isInstance(featureConfig)) {
            String nameSpace = "";
            var nextHolder = configuredFeature.getSubFeatures().findFirst();
            if (nextHolder.isEmpty()) {
                return false;
            }
            final ConfiguredFeature<?, ?> nextConfiguredFeature = nextHolder.get().value();
            final FeatureConfiguration nextFeatureConfig = nextConfiguredFeature.config();
            final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(nextConfiguredFeature.feature());
            if (featureRegistryName != null) {
                nameSpace = featureRegistryName.getNamespace();
            }
            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && !nameSpace.isEmpty() &&
                featureCancellations.shouldCancelNamespace(nameSpace)) {
                return true;
            } else if (nextFeatureConfig instanceof RandomFeatureConfiguration randomNext) {
                return this.doesContainTrees(randomNext, featureCancellations);
            }
        }
        return false;
    }

    private boolean doesContainTrees(RandomFeatureConfiguration featureConfig, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        for (WeightedPlacedFeature feature : featureConfig.features()) {
            final PlacedFeature currentPlacedFeature = feature.feature().value();
            var configured = currentPlacedFeature.getFeatures().findFirst();
            if (configured.isEmpty()) {
                continue;
            }
            final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configured.get().value().feature());
            if (this.treeFeatureConfigClass.isInstance(configured.get().value().config()) && featureRegistryName != null &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
                return true;
            }
        }
        return false;
    }

}
