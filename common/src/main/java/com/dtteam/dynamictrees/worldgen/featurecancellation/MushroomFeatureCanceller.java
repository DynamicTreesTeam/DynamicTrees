package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

import java.util.stream.Stream;


public class MushroomFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> mushroomFeatureConfigClass;

    public MushroomFeatureCanceller(final Identifier registryName, final Class<T> mushroomFeatureConfigClass) {
        super(registryName);
        this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature, final BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configuredFeature.feature());

        if (featureRegistryName == null) {
            return false;
        }

        // Mushrooms come in RandomBooleanFeatureConfiguration to select between brown and red.
        if (!(configuredFeature.config() instanceof RandomFeatureConfiguration randomConfig)) {
            return false;
        }

        return getConfigs(randomConfig).anyMatch(this.mushroomFeatureConfigClass::isInstance) &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
    }

    private Stream<FeatureConfiguration> getConfigs(final RandomFeatureConfiguration twoFeatureConfig) {
        return twoFeatureConfig.getFeatures().map(ConfiguredFeature::config);
    }
}