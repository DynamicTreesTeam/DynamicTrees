package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import net.minecraft.resources.ResourceLocation;

public class BiomePredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<GenFeatureConfiguration> GEN_FEATURE = ConfigurationProperty.property("gen_feature", GenFeatureConfiguration.class);

    public BiomePredicateGenFeature(ResourceLocation registryName) {
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
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final boolean worldGen = context.isWorldGen();
        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DTTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        if (!(configuration.get(ONLY_WORLD_GEN) && !worldGen) && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            return configurationToPlace.generate(Type.POST_GENERATION, context);
        }

        return false;
    }

}