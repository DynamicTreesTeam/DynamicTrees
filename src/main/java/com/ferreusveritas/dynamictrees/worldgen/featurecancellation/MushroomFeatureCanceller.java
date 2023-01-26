package com.ferreusveritas.dynamictrees.worldgen.featurecancellation;

import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

import java.util.Set;
import java.util.stream.Stream;


public class MushroomFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> mushroomFeatureConfigClass;

    public MushroomFeatureCanceller(final ResourceLocation registryName, final Class<T> mushroomFeatureConfigClass) {
        super(registryName);
        this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, Set<String> namespaces) {
        final ResourceLocation featureName = configuredFeature.feature().getRegistryName();

        if (featureName == null) {
            return false;
        }

        // Mushrooms come in RandomBooleanFeatureConfiguration to select between brown and red.
        if (!(configuredFeature.config() instanceof RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration)) {
            return false;
        }

        return getConfigs(randomBooleanFeatureConfiguration).anyMatch(this.mushroomFeatureConfigClass::isInstance) &&
                namespaces.contains(featureName.getNamespace());
    }

    private Stream<FeatureConfiguration> getConfigs(final RandomBooleanFeatureConfiguration twoFeatureConfig) {
        return twoFeatureConfig.getFeatures().map(ConfiguredFeature::config);
    }
}
