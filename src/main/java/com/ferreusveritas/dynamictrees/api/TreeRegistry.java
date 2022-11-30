package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.item.DendroPotion;
import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Returns all {@link Species} registry names for which the {@link Species} if marked {@code transformable}.
     *
     * @return A {@link List<ResourceLocation>} for which their {@link Species} can be transformed to other {@link
     * Species}.
     */
    public static List<ResourceLocation> getTransformableSpeciesLocations() {
        return Species.REGISTRY.getRegistryNames().stream().filter(resLoc ->
                findSpecies(resLoc).isTransformable()).collect(Collectors.toList());
    }

    /**
     * Returns all {@link Species} which are marked {@code transformable}.
     *
     * @return A {@link List<Species>} which can be transformed to other {@link Species}.
     */
    public static List<Species> getTransformableSpecies() {
        return getTransformableSpeciesLocations().stream().map(TreeRegistry::findSpecies).collect(Collectors.toList());
    }

    /**
     * Returns a {@link List} of all transformable {@link Species} which can be transformed by a {@link DendroPotion}.
     * This includes any {@link Species} which has a {@link Seed} and is not the common species (or whose seed is
     * common).
     *
     * @return All {@link Species} which are marked {@code transformable} and have their own {@link Seed}.
     */
    public static List<Species> getPotionTransformableSpecies() {
        return getTransformableSpecies().stream().filter(species -> species.hasSeed() &&
                (!species.isCommonSpecies() || species.isSeedCommon())).collect(Collectors.toList());
    }

    //////////////////////////////
    // SAPLING HANDLING
    //////////////////////////////

    public final static Map<BlockState, Species> SAPLING_REPLACERS = new HashMap<>();

    public static void registerSaplingReplacer(BlockState state, Species species) {
        SAPLING_REPLACERS.put(state, species);
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
        return processResLoc(new ResourceLocation(resLocStr));
    }

    /**
     * Parses resource location and  processes it via {@link #processResLoc(ResourceLocation)}. If it could not be
     * parsed, returns {@link DTTrees#NULL}.
     *
     * @param resourceLocationString The {@link ResourceLocation} {@link String} to parse.
     * @return The parsed and processed {@link ResourceLocation} object.
     */
    public static ResourceLocation parseResLoc(final String resourceLocationString) {
        return Optional.ofNullable(ResourceLocation.tryParse(resourceLocationString))
                .orElse(DTTrees.NULL);
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
