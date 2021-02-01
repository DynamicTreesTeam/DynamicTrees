package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

/**
 * @author Harley O'Connor
 */
public final class WorldGenEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addDynamicTrees (final BiomeLoadingEvent event) {
        event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_FEATURE.withConfiguration(new NoFeatureConfig()));
    }

    /**
     * This is not an ideal way of removing trees, but it's the best way I've currently found.
     * It currently loops through the vegetal features of the current biome and removes them if they are found to contain trees.
     *
     * @param event The biome loading event.
     */
    @SubscribeEvent
    public void removeVanillaTrees(final BiomeLoadingEvent event) {
        final ResourceLocation biomeResLoc = event.getName();

        if (biomeResLoc == null) return;

        // Gets the Json tree canceller.
        ITreeCanceller treeCanceller = WorldGenRegistry.getJsonTreeCanceller();

        if (!treeCanceller.shouldCancelFeatures(biomeResLoc))
            return;

        // TODO: Make a tree type in tree canceller for handling this.
        if (event.getCategory() == Biome.Category.NETHER) {
            this.removeFungi(event, biomeResLoc, treeCanceller); // This removes any feature whose config extends HugeFungusConfig.
        } else {
            this.removeTrees(event, biomeResLoc, treeCanceller); // This removes any feature whose config extends BaseTreeFeatureConfig.
        }
    }

    private void removeFungi(final BiomeLoadingEvent event, final ResourceLocation biomeResLoc, final ITreeCanceller treeCanceller) {
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            final ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();
            if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

            final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) configuredFeature.config).feature.get();
            return nextConfiguredFeature.config instanceof HugeFungusConfig &&
                    treeCanceller.shouldCancelFeature(biomeResLoc, nextConfiguredFeature.feature.getRegistryName());
        });
    }

    private void removeTrees(final BiomeLoadingEvent event, final ResourceLocation biomeResLoc, final ITreeCanceller treeCanceller) {
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            final ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();

            if (!(configuredFeature.config instanceof DecoratedFeatureConfig)) return false;

            final IFeatureConfig featureConfig = ((DecoratedFeatureConfig) configuredFeature.config).feature.get().config;

            /*  The following code removes vanilla trees from the biome's generator.
                There may be some problems as MultipleRandomFeatureConfigs store can other features too,
                so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
                but one (unclean) solution may be to add the non-tree features back to the generator. */

            if (featureConfig instanceof MultipleRandomFeatureConfig) {
                // Removes feature if it contains trees.
                return doesContainTrees((MultipleRandomFeatureConfig) featureConfig, biomeResLoc, treeCanceller);
            } else if (featureConfig instanceof DecoratedFeatureConfig) {
                final ConfiguredFeature<?, ?> nextConfiguredFeature = ((DecoratedFeatureConfig) featureConfig).feature.get();
                final IFeatureConfig nextFeatureConfig = nextConfiguredFeature.config;

                if (nextFeatureConfig instanceof BaseTreeFeatureConfig && treeCanceller.shouldCancelFeature(biomeResLoc,
                        nextConfiguredFeature.feature.getRegistryName())) {
                    return true; // Removes any individual trees.
                } else if (nextFeatureConfig instanceof MultipleRandomFeatureConfig) {
                    // Removes feature if it contains trees.
                    return doesContainTrees((MultipleRandomFeatureConfig) nextFeatureConfig, biomeResLoc, treeCanceller);
                }
            }

            return false;
        });
    }

    /**
     * Checks if the given {@link MultipleRandomFeatureConfig} contains trees.
     *
     * @param featureConfig The MultipleRandomFeatureConfig to check.
     * @return True if trees were found.
     */
    private boolean doesContainTrees (MultipleRandomFeatureConfig featureConfig, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        for (ConfiguredRandomFeatureList feature : featureConfig.features) {
            ConfiguredFeature<?, ?> currentConfiguredFeature = feature.feature.get();
            if (currentConfiguredFeature.config instanceof BaseTreeFeatureConfig && treeCanceller.shouldCancelFeature(biomeResLoc,
                    currentConfiguredFeature.feature.getRegistryName()))
                return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onTreeCancelRegistry(TreeCancelRegistryEvent event) {
        event.getTreeCanceller().registerCancellations("minecraft", Collections.singletonList("minecraft"));
    }

}
