package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author Harley O'Connor
 */
public final class WorldGenEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addDynamicTrees (final BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND) return;

//        event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_FEATURE.withConfiguration(new NoFeatureConfig()));
    }

    /**
     * This is not an ideal way of removing trees, but it's the best way I've currently found.
     * It currently loops through the vegetal features of the current biome and removes them if they are found to contain trees.
     *
     * @param event The biome loading event.
     */
    @SubscribeEvent
    public void removeVanillaTrees(final BiomeLoadingEvent event) {
        if (event.getName() == null) return;

        // Check the current biome is from Minecraft.
        if (!event.getName().getNamespace().equals("minecraft") || event.getCategory().equals(Biome.Category.THEEND)) return;

        // Loop through all vegetal features of current biome and remove if algorithm determines it contains a tree feature.
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            final ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();

            if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

            final IFeatureConfig featureConfig = ((DecoratedFeatureConfig) configuredFeature.config).feature.get().config;

            /*  The following code removes vanilla trees from the biome's generator.
                There may be some problems as MultipleRandomFeatureConfigs store can other features too,
                so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
                but one (unclean) solution may be to add the non-tree features back to the generator. */

            if (featureConfig instanceof HugeFungusConfig) {
                return true; // Removes huge fungus from the nether.
            } else if (featureConfig instanceof MultipleRandomFeatureConfig) {
                // Removes feature if it contains trees.
                return doesContainTrees((MultipleRandomFeatureConfig) featureConfig);
            } else if (featureConfig instanceof DecoratedFeatureConfig) {
                final IFeatureConfig nextFeatureConfig = ((DecoratedFeatureConfig) featureConfig).feature.get().config;

                if (nextFeatureConfig instanceof BaseTreeFeatureConfig) {
                    return true; // Removes any individual trees.
                } else if (nextFeatureConfig instanceof MultipleRandomFeatureConfig) {
                    // Removes feature if it contains trees.
                    return doesContainTrees((MultipleRandomFeatureConfig) nextFeatureConfig);
                }
            }

            return false;
        });
    }

    /**
     * Checks if the given MultipleRandomFeatureConfig contains trees.
     *
     * @param featureConfig The MultipleRandomFeatureConfig to check.
     * @return True if trees were found.
     */
    private boolean doesContainTrees (MultipleRandomFeatureConfig featureConfig) {
        for (ConfiguredRandomFeatureList feature : featureConfig.features) {
            if (feature.feature.get().config instanceof BaseTreeFeatureConfig)
                return true;
        }
        return false;
    }

}
