package com.ferreusveritas.dynamictrees.worldgen.featurecancellation;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;


public class MushroomFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {
    private final Class<T> mushroomFeatureConfigClass;

    public MushroomFeatureCanceller(final ResourceLocation registryName, final Class<T> mushroomFeatureConfigClass) {
        super(registryName);
        this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature, final BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final ResourceLocation featureRegistryName = ForgeRegistries.FEATURES.getKey(configuredFeature.feature());

        if (featureRegistryName == null) {
            return false;
        }

        // Mushrooms come in RandomBooleanFeatureConfiguration to select between brown and red.
        if (!(configuredFeature.config() instanceof RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration)) {
            return false;
        }

        return getConfigs(randomBooleanFeatureConfiguration).anyMatch(this.mushroomFeatureConfigClass::isInstance) &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
    }

    private Stream<FeatureConfiguration> getConfigs(final RandomBooleanFeatureConfiguration twoFeatureConfig) {
        return twoFeatureConfig.getFeatures().map(ConfiguredFeature::config);
    }
}