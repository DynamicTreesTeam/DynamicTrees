package com.ferreusveritas.dynamictrees.worldgen.biomemodifiers;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class AddDynamicTreesBiomeModifier implements BiomeModifier {
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && DTConfigs.WORLD_GEN.get()) {
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_PLACED_FEATURE.getHolder().orElseThrow());
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return DTRegistries.ADD_DYNAMIC_TREES_BIOME_MODIFIER.get();
    }
}