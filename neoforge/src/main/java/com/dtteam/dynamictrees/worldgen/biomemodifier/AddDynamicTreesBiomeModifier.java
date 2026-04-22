package com.dtteam.dynamictrees.worldgen.biomemodifier;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class AddDynamicTreesBiomeModifier implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && DTConfigs.SERVER.worldGen.get()) {
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            var placedFeatures = server.registryAccess().get(Registries.PLACED_FEATURE);
            if (placedFeatures.isEmpty()) return;
            placedFeatures.get().value().get(DTRegistries.CAVE_ROOTED_TREE_PLACED_FEATURE).ifPresent(
                    feature -> generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature));
            placedFeatures.get().value().get(DTRegistries.DYNAMIC_TREE_PLACED_FEATURE).ifPresent(
                    feature -> generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return NeoForgeRegistryLoader.ADD_DYNAMIC_TREES_BIOME_MODIFIER.get();
    }

}