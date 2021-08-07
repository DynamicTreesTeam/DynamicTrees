package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import net.minecraft.util.ResourceLocation;

public class BiomePredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<ConfiguredGenFeature<GenFeature>> GEN_FEATURE = ConfigurationProperty.property("gen_feature", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS);

    public BiomePredicateGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(BIOME_PREDICATE, GEN_FEATURE, ONLY_WORLD_GEN);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(BIOME_PREDICATE, i -> true)
                .with(GEN_FEATURE, ConfiguredGenFeature.NULL_CONFIGURED_FEATURE)
                .with(ONLY_WORLD_GEN, false);
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature<GenFeature> configuration, PostGenerationContext context) {
        final boolean worldGen = context.isWorldGen();
        final ConfiguredGenFeature<GenFeature> configuredGenFeatureToPlace = configuration.get(GEN_FEATURE);

        if (configuration.getGenFeature().getRegistryName().equals(DTTrees.NULL)) { // If the gen feature was null, do nothing.
            return false;
        }

        final GenFeature genFeatureToPlace = configuredGenFeatureToPlace.getGenFeature();

        if (!(configuration.get(ONLY_WORLD_GEN) && !worldGen) && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            return genFeatureToPlace.generate(
                    configuredGenFeatureToPlace,
                    Type.POST_GENERATION,
                    context
            );
        }

        return false;
    }

}
