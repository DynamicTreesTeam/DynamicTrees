package com.dtteam.dynamictrees.worldgen.biomemodifier;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class AddDynamicTreesBiomeModifier implements BiomeModifier {

    private static final Logger LOGGER = LogManager.getLogger();

    private Optional<Registry<PlacedFeature>> featureRegistry;

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && DTConfigs.SERVER.worldGen.get()) {
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
            net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            featureRegistry = server.registryAccess().lookup(Registries.PLACED_FEATURE);

            addFeature(generationSettings, DTRegistries.CAVE_ROOTED_TREE_PLACED_FEATURE);
            addFeature(generationSettings, DTRegistries.DYNAMIC_TREE_PLACED_FEATURE);

        }
    }

    private void addFeature(BiomeGenerationSettingsBuilder generationSettings, ResourceKey<PlacedFeature> featureKey) {
        if (featureRegistry.isEmpty()) {
            LOGGER.error("Failed to load the PlacedFeature Registry.");
            return;
        }
        featureRegistry.get().get(featureKey).ifPresentOrElse(
                feature ->
                generationSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature),
                () -> LOGGER.error("Could not add {} feature to biome modifiers. Feature was not found.", featureKey.toString())
        );
    }


    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return NeoForgeRegistryLoader.ADD_DYNAMIC_TREES_BIOME_MODIFIER.get();
    }

}