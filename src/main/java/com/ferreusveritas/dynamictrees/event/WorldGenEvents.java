package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author Harley O'Connor
 */
public final class WorldGenEvents {

//    private static final List<String> knownTreeFeatureNames = Arrays.asList("oak", "dark_oak", "birch", "acacia", "spruce", "pine", "jungle_tree", "fancy_oak", "jungle_tree_no_vine", "mega_jungle_tree", "mega_spruce", "mega_pine", "super_birch_bees_0002", "swamp_tree", "oak_bees_0002", "oak_bees_002", "oak_bees_005", "birch_bees_0002", "birch_bees_002", "birch_bees_005", "fancy_oak_bees_0002", "fancy_oak_bees_002", "fancy_oak_bees_005", "forest_flower_trees", "trees_shattered_savanna", "trees_savanna", "trees_birch", "trees_mountain_edge", "trees_mountain", "trees_water", "birch_other", "plain_vegetation", "trees_jungle_edge", "trees_giant_spruce", "trees_giant", "trees_jungle");

    /**
     * This is not an ideal way of removing trees, but it's the best way I've currently found.
     * It currently loops through the vegetal features of the current biome and removes them if they are found to contain trees.
     *
     * @param event
     */
    @SubscribeEvent
    public void removeVanillaTrees(final BiomeLoadingEvent event) {
        if (event.getName() == null) return;

        // Check the current biome is from Minecraft.
        if (!event.getName().getNamespace().equals("minecraft")) return;

        // Loop through all vegetal features of current biome and remove if algorithm determines it contains a tree feature.
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            final ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();
            final IFeatureConfig featureConfig = ((DecoratedFeatureConfig) configuredFeature.config).feature.get().config;

            /*  The following code currently only seems to remove trees from the dark forest.
                Since the features of a MultipleRandomFeature are stored in an immutable list, the entire thing must be removed.
                This may be a problem as mushrooms are stored in the same list, so they are currently removed from worldgen too.
                One (unclean) solution may be to add the non-tree features back to the generator. */

            if (featureConfig instanceof MultipleRandomFeatureConfig) {
                for (ConfiguredRandomFeatureList feature : ((MultipleRandomFeatureConfig) featureConfig).features) {
                    if (feature.feature.get().config instanceof BaseTreeFeatureConfig)
                        return true;
                }
            }

            return false;
        });
    }

}
