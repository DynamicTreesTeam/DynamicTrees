package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;

import java.util.Arrays;
import java.util.List;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling
 * mushroom features. It cancels any features that have a config that extends the given class, and are
 * inside a {@link TwoFeatureChoiceConfig}.
 *
 * @author Harley O'Connor
 */
public class MushroomFeatureCanceller<T extends IFeatureConfig> implements ITreeFeatureCanceller {

    private final Class<T> fungusFeatureConfigClass;

    public MushroomFeatureCanceller(Class<T> fungusFeatureConfigClass) {
        this.fungusFeatureConfigClass = fungusFeatureConfigClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

        ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) configuredFeature.config).feature.get();

        // Mushrooms come in TwoFeatureChoiceConfigs to select between brown and red.
        if (!(nextConfiguredFeature.config instanceof TwoFeatureChoiceConfig)) return false;

        return getConfigs((TwoFeatureChoiceConfig) nextConfiguredFeature.config).stream().anyMatch(this.fungusFeatureConfigClass::isInstance) &&
                treeCanceller.shouldCancelFeature(biomeResLoc, nextConfiguredFeature.feature.getRegistryName());
    }

    private List<IFeatureConfig> getConfigs (TwoFeatureChoiceConfig twoFeatureConfig) {
        return Arrays.asList(twoFeatureConfig.field_227285_a_.get().config, twoFeatureConfig.field_227286_b_.get().config);
    }

}
