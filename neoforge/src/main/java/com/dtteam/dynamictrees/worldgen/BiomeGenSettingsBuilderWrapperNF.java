package com.dtteam.dynamictrees.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;

import java.util.List;

public class BiomeGenSettingsBuilderWrapperNF extends BiomeGenSettingsBuilderWrapper{

    private final BiomeGenerationSettingsBuilder settingsBuilder;

    public BiomeGenSettingsBuilderWrapperNF(BiomeGenerationSettingsBuilder settingsBuilder) {
        this.settingsBuilder = settingsBuilder;
    }

    @Override
    public BiomeGenerationSettings.PlainBuilder getPlainBuilder() {
        return settingsBuilder;
    }

    @Override
    public List<Holder<PlacedFeature>> getFeatures(GenerationStep.Decoration stage) {
        return settingsBuilder.getFeatures(stage);
    }

    @Override
    public List<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving stage) {
        return settingsBuilder.getCarvers(stage);
    }
}
