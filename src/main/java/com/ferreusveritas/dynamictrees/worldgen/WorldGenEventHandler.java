package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles events relating to world gen, including adding {@link DynamicTreeFeature} objects to biomes, and registering
 * and calling {@link FeatureCanceller} objects.
 *
 * @author Harley O'Connor
 */
public final class WorldGenEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addDynamicTrees(final BiomeLoadingEvent event) {
        event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_PLACED_FEATURE.getHolder().get());
    }

    /**
     * This is not an ideal way of removing trees, but it's the best way I've currently found. It currently loops
     * through the vegetal features of the current biome and removes them if they are found to contain trees.
     *
     * @param event The biome loading event.
     */
    @SubscribeEvent
    public void removeVanillaTrees(final BiomeLoadingEvent event) {
        // Currently, any mods that don't create their own Feature for trees will have it removed (if they use a ConfiguredFeature that uses Feature.TREE).
        // This may just have to be an unfortunate consequence to Dynamic Trees for now, as without making an overly complex system I can't see any other
        // way of removing features to rectify this.

        final ResourceLocation biomeName = event.getName();
        if (biomeName == null) {
            return;
        }

        BiomeDatabases.getDefault().getEntry(biomeName).getFeatureCancellation().cancelFeatures(event.getGeneration());
    }

}
