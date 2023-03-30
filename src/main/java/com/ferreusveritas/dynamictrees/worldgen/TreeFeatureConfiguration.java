package com.ferreusveritas.dynamictrees.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TreeFeatureConfiguration(BiomeDatabase.EntryReader biomeEntry) implements FeatureConfiguration {

    public static final Codec<TreeFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(BiomeDatabase.EntryReader.CODEC.fieldOf("biome").forGetter(TreeFeatureConfiguration::biomeEntry))
                    .apply(instance, TreeFeatureConfiguration::new)
    );

}
