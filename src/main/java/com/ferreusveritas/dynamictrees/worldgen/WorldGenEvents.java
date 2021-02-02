package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.canceller.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.canceller.ITreeCanceller;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeFeatureCancellerRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
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

        // Ensure tree feature cancellers have been registered.
        TreeFeatureCancellerRegistry.registerCancellers();

        // Loop through all vegetal features and remove if found to contain trees.
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            // Go through each canceller for the current biome and remove the current feature if it shouldCancel returns true.
            for (ITreeFeatureCanceller canceller : treeCanceller.getFeatureCancellers(biomeResLoc)) {
                if (canceller.shouldCancel(configuredFeatureSupplier.get(), biomeResLoc, treeCanceller))
                    return true;
            }
            return false;
        });
    }

    ///////////////////////////////////////////
    // Registries
    ///////////////////////////////////////////

    @SubscribeEvent
    public void onTreeCancelRegistry(TreeCancelRegistryEvent event) {
        // TODO: Make a way of applying to a biome type with a specific registry name so we only run fungus cancellers in the nether and only run normal tree cancellers in the overworld.

        // This registers the cancellation of all tree features with the namespace "minecraft" from all biomes with the namespace "minecraft".
        // Or, in other words, cancels all vanilla Minecraft trees from vanilla Minecraft biomes.
        event.getTreeCanceller().register(DynamicTrees.MINECRAFT_ID, Collections.singletonList(DynamicTrees.MINECRAFT_ID),
                Arrays.asList(TreeFeatureCancellerRegistry.TREE_CANCELLER, TreeFeatureCancellerRegistry.FUNGUS_CANCELLER));
    }

    @SubscribeEvent
    public void onTreeFeatureCancelRegistry(TreeFeatureCancellerRegistry.TreeFeatureCancellerRegistryEvent event) {
        final TreeFeatureCancellerRegistry registry = event.getFeatureCancellerRegistry();

        // This registers default tree feature canceller, which will cancel any features if their config extends BaseTreeFeatureConfig.
        registry.register(TreeFeatureCancellerRegistry.TREE_CANCELLER, new TreeFeatureCanceller<>(BaseTreeFeatureConfig.class));

        // This registers the tree feature canceller for fungus, which will cancel any features if their config extends HugeFungusConfig.
        registry.register(TreeFeatureCancellerRegistry.FUNGUS_CANCELLER, new FungusFeatureCanceller<>(HugeFungusConfig.class));
    }

}
