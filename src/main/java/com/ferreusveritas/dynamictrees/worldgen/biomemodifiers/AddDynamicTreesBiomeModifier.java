package com.ferreusveritas.dynamictrees.worldgen.biomemodifiers;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.DTFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AddDynamicTreesBiomeModifier implements BiomeModifier {
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && DTConfigs.WORLD_GEN.get()) {
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            var placedFeatures = ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, placedFeatures.getHolderOrThrow(DTFeatures.DYNAMIC_TREE_PLACED_FEATURE));
            generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, placedFeatures.getHolderOrThrow(DTFeatures.CAVE_ROOTED_TREE_PLACED_FEATURE));
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return DTRegistries.ADD_DYNAMIC_TREES_BIOME_MODIFIER.get();
    }
}