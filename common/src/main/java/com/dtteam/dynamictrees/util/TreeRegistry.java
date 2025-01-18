package com.dtteam.dynamictrees.util;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Contains various utility functions relating to {@link Object}s with a {@link SimpleRegistry}.
 *
 * @author ferreusveritas
 */
public final class TreeRegistry {

    private TreeRegistry() {
    }

    //////////////////////////////
    // SPECIES REGISTRY
    //////////////////////////////

    public static Species findSpecies(final String name) {
        return findSpecies(getResLoc(name));
    }

    public static Species findSpecies(final ResourceLocation name) {
        return Species.REGISTRY.get(name);
    }

    /**
     * Searches first for the full tree name.  If that fails then it will find the first tree matching the simple name
     * and return it instead otherwise null
     *
     * @param name The name of the tree.  Either the simple name or the full name
     * @return The tree that was found or null if not found
     */
    public static Species findSpeciesSloppy(final String name) {
        final ResourceLocation resourceLocation = getResLoc(name);

        // Search specific domain first.
        if (Species.REGISTRY.has(resourceLocation)) {
            return findSpecies(resourceLocation);
        }

        // Search all domains.
        for (Species species : Species.REGISTRY) {
            if (species.getRegistryName().getPath().equals(resourceLocation.getPath())) {
                return species;
            }
        }

        return Species.NULL_SPECIES;
    }

    /**
     * Returns a new {@link ArrayList<ResourceLocation>} from the {@link Species#REGISTRY} values.
     *
     * @return A new {@link List} from the {@link Species#REGISTRY}.
     */
    public static List<ResourceLocation> getSpeciesDirectory() {
        return new ArrayList<>(Species.REGISTRY.getRegistryNames());
    }

    //////////////////////////////
    // SAPLING HANDLING
    //////////////////////////////

    public final static Map<Block, Species> SAPLING_REPLACERS = new HashMap<>();

    public static void registerSaplingReplacer(BlockState state, Species species) {
        SAPLING_REPLACERS.put(state.getBlock(), species);
    }

    //////////////////////////////
    // CELL KIT HANDLING
    //////////////////////////////

    public static CellKit findCellKit(String name) {
        return findCellKit(getResLoc(name));
    }

    public static CellKit findCellKit(ResourceLocation name) {
        return CellKit.REGISTRY.get(name);
    }

    //////////////////////////////
    // GROWTH LOGIC KIT HANDLING
    //////////////////////////////

    public static GrowthLogicKit findGrowthLogicKit(final String name) {
        return findGrowthLogicKit(getResLoc(name));
    }

    public static GrowthLogicKit findGrowthLogicKit(final ResourceLocation name) {
        return GrowthLogicKit.REGISTRY.get(name);
    }

    public static ResourceLocation getResLoc(final String resLocStr) {
        return processResLoc(ResourceLocation.parse(resLocStr));
    }

    /**
     * Parses resource location and  processes it via {@link #processResLoc(ResourceLocation)}. If it could not be
     * parsed, returns {@link DynamicTrees#NULL}.
     *
     * @param resourceLocationString The {@link ResourceLocation} {@link String} to parse.
     * @return The parsed and processed {@link ResourceLocation} object.
     */
    public static ResourceLocation parseResLoc(final String resourceLocationString) {
        return Optional.ofNullable(ResourceLocation.tryParse(resourceLocationString))
                .orElse(DynamicTrees.NULL);
    }

    /**
     * Changes namespace of resource location to "dynamictrees" as a default if it is set to Minecraft. This is safe
     * since Minecraft won't (or shouldn't) have used any of our registries.
     *
     * @param resourceLocation The {@link ResourceLocation} to parse.
     * @return The {@link ResourceLocation} object.
     */
    public static ResourceLocation processResLoc(final ResourceLocation resourceLocation) {
        return DynamicTrees.MINECRAFT.equals(resourceLocation.getNamespace()) ?
                DynamicTrees.location(resourceLocation.getPath()) : resourceLocation;
    }

}
