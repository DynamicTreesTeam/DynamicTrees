package com.ferreusveritas.dynamictrees.worldgen.cancellers;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling fungus features.
 * It cancels any features that have a config that extends the given class.
 *
 * @param <T> An {@link FeatureConfiguration} which should be cancelled.
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends FeatureConfiguration> extends FeatureCanceller {

    private final Class<T> fungusFeatureConfigClass;

    public FungusFeatureCanceller(final ResourceLocation registryName, final Class<T> fungusFeatureConfigClass) {
        super(registryName);
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }


    //todo: add support for this
    @Override
    public boolean shouldCancel(PlacedFeature configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
//        if (!(configuredFeature.placem instanceof FeatureConfiguration)) {
//            return false;
//        }
//
//        final ConfiguredFeature<?, ?> nextConfiguredFeature = ((FeatureConfiguration) configuredFeature.config).feature.get();
//        final ResourceLocation featureRegistryName = nextConfiguredFeature.feature.getRegistryName();
//
//        return this.fungusFeatureConfigClass.isInstance(nextConfiguredFeature.config) && featureRegistryName != null &&
//                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
        return  configuredFeature.getFeatures().findFirst().get().feature instanceof HugeFungusFeature;
    }

}
