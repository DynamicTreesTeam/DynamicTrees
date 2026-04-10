package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.systems.genfeature.context.FullGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostRotContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.resources.Identifier;

public class BiomePredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<GenFeatureConfiguration> GEN_FEATURE = ConfigurationProperty.property("gen_feature", GenFeatureConfiguration.class);

    public BiomePredicateGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(BIOME_PREDICATE, GEN_FEATURE, ONLY_WORLD_GEN);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(BIOME_PREDICATE, i -> true)
                .with(GEN_FEATURE, GenFeatureConfiguration.getNull())
                .with(ONLY_WORLD_GEN, false);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return configuration.get(GEN_FEATURE).shouldApply(species);
    }

    //Pre-generation does not have access to biome
    @Override
    protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
        final boolean worldGen = context.isWorldGen();
        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DynamicTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        if (!(configuration.get(ONLY_WORLD_GEN) && !worldGen) && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            return configurationToPlace.generate(Type.FULL, context);
        }

        return false;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final boolean worldGen = context.isWorldGen();
        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DynamicTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        if (!(configuration.get(ONLY_WORLD_GEN) && !worldGen) && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            return configurationToPlace.generate(Type.POST_GENERATION, context);
        }

        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DynamicTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        if (!configuration.get(ONLY_WORLD_GEN) && configuration.get(BIOME_PREDICATE).test(context.level().getBiome(context.pos()))) {
            return configurationToPlace.generate(Type.POST_GROW, context);
        }

        return false;
    }

    @Override
    protected boolean postRot(GenFeatureConfiguration configuration, PostRotContext context) {
        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DynamicTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        if (!configuration.get(ONLY_WORLD_GEN) && configuration.get(BIOME_PREDICATE).test(context.level().getBiome(context.pos()))) {
            return configurationToPlace.generate(Type.POST_ROT, context);
        }

        return false;
    }
}