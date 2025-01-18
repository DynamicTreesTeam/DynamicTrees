package com.dtteam.dynamictrees.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public abstract class BiomeGenSettingsBuilderWrapper {

    public abstract BiomeGenerationSettings.PlainBuilder getPlainBuilder ();

    public abstract List<Holder<PlacedFeature>> getFeatures(GenerationStep.Decoration stage);

    public abstract List<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving stage);

}
