package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.systems.genfeature.context.*;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class RandomPredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<GenFeatureConfiguration> GEN_FEATURE = ConfigurationProperty.property("gen_feature", GenFeatureConfiguration.class);

    public RandomPredicateGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(PLACE_CHANCE, GEN_FEATURE, ONLY_WORLD_GEN);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(PLACE_CHANCE, 0.5f)
                .with(GEN_FEATURE, GenFeatureConfiguration.getNull())
                .with(ONLY_WORLD_GEN, false);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return configuration.get(GEN_FEATURE).shouldApply(species);
    }

    @Override
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        if (configuration.get(ONLY_WORLD_GEN) && !context.isWorldGen() ||
                Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, do nothing.
            return context.pos();
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        if (!configuration.getGenFeature().isValid()) return context.pos();
        return configurationToPlace.generate(Type.PRE_GENERATION, context);
    }

    @Override
    protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
        if (configuration.get(ONLY_WORLD_GEN) && !context.isWorldGen() ||
                Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.generate(Type.FULL, context);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (configuration.get(ONLY_WORLD_GEN) && !context.isWorldGen() ||
                Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.generate(Type.POST_GENERATION, context);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (configuration.get(ONLY_WORLD_GEN)
                || Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, or its only for world gen, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.generate(Type.POST_GROW, context);
    }

    @Override
    protected boolean postRot(GenFeatureConfiguration configuration, PostRotContext context) {
        if (configuration.get(ONLY_WORLD_GEN)
                || Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, or its only for world gen, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.generate(Type.POST_ROT, context);
    }
}
