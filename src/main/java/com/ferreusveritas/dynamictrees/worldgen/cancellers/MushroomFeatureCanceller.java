package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Arrays;
import java.util.List;


public class MushroomFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> mushroomFeatureConfigClass;

    public MushroomFeatureCanceller(final ResourceLocation registryName, final Class<T> mushroomFeatureConfigClass) {
        super(registryName);
        this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
    }
//todo: check if this is correct
    @Override
    public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature, final BiomePropertySelectors.FeatureCancellations featureCancellations) {
//        if (!(configuredFeature.config instanceof DecoratedFeatureConfiguration)) {
//            return false;
//        }
//
//        final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfiguration) configuredFeature.config).feature.get();
//        final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();
//
//        if (featureRegistryName == null) {
//            return false;
//        }
//
//        // Mushrooms come in TwoFeatureChoiceConfigs to select between brown and red.
//        if (!(nextConfiguredFeature.config instanceof RandomBooleanFeatureConfiguration)) {
//            return false;
//        }
//
//        return getConfigs((RandomBooleanFeatureConfiguration) nextConfiguredFeature.config).stream().anyMatch(this.mushroomFeatureConfigClass::isInstance) &&
//                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
        return false;//configuredFeature.getFeatures().findFirst().get().feature instanceof AbstractHugeMushroomFeature;
    }

//    private List<FeatureConfiguration> getConfigs(final RandomBooleanFeatureConfiguration twoFeatureConfig) {
//        return Arrays.asList(twoFeatureConfig.featureTrue.get().config, twoFeatureConfig.featureFalse.get().config);
//    }

}
