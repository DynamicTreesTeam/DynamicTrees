package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.ResourceLocation;

public class RandomPredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<ConfiguredGenFeature<GenFeature>> GEN_FEATURE = ConfigurationProperty.property("gen_feature", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS);

    public RandomPredicateGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(PLACE_CHANCE, GEN_FEATURE, ONLY_WORLD_GEN);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(PLACE_CHANCE, 0.5f)
                .with(GEN_FEATURE, ConfiguredGenFeature.NULL_CONFIGURED_FEATURE)
                .with(ONLY_WORLD_GEN, false);
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature<GenFeature> configuration, PostGenerationContext context) {
        if (configuration.get(ONLY_WORLD_GEN) && !context.isWorldGen() ||
                Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, do nothing.
            return false;
        }

        final ConfiguredGenFeature<GenFeature> configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.getGenFeature().postGenerate(configurationToPlace, context);
    }

    @Override
    protected boolean postGrow(ConfiguredGenFeature<GenFeature> configuration, PostGrowContext context) {
        if (configuration.get(ONLY_WORLD_GEN)
                || Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, or its only for world gen, do nothing.
            return false;
        }

        final ConfiguredGenFeature<GenFeature> configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.getGenFeature().postGrow(configurationToPlace, context);
    }

}
