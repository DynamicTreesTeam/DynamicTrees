package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.dropcreators.StorageDropCreator;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* A registry for all of the dynamic trees. Use this for this mod or other mods.
*
* @author ferreusveritas
*/
public class TreeRegistry {

	public static final IDropCreatorStorage GLOBAL_DROP_CREATOR_STORAGE = new StorageDropCreator();

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
	 * Searches first for the full tree name.  If that fails then it
	 * will find the first tree matching the simple name and return it instead otherwise null
	 *
	 * @param name The name of the tree.  Either the simple name or the full name
	 * @return The tree that was found or null if not found
	 */
	public static Species findSpeciesSloppy(final String name) {

		final ResourceLocation resourceLocation = getResLoc(name);

		//Search specific domain first
		if (Species.REGISTRY.has(resourceLocation)) {
			return findSpecies(resourceLocation);
		}

		//Search all domains
		for (Species species : Species.REGISTRY) {
			if (species.getRegistryName().getPath().equals(resourceLocation.getPath())) {
				return species;
			}
		}

		return Species.NULL_SPECIES;
	}

	public static List<ResourceLocation> getSpeciesDirectory() {
		return new ArrayList<>(Species.REGISTRY.getRegistryNames());
	}

	/**
	 * @return A list of resource locations for species which can be transformed to other species.
	 */
	public static List<ResourceLocation> getTransformableSpeciesLocations() {
		final List<ResourceLocation> species = getSpeciesDirectory();
		species.removeIf(resLoc -> !findSpecies(resLoc).isTransformable());
		return species;
	}

	/**
	 * @return All species which can be transformed.
	 */
	public static List<Species> getTransformableSpecies() {
		final List<Species> species = new ArrayList<>();
		getTransformableSpeciesLocations().forEach(speciesLoc -> species.add(findSpecies(speciesLoc)));
		return species;
	}

	/**
	 * @return All species which can be transformed and have their own seed (so should have a potion recipe created).
	 */
	public static List<Species> getPotionTransformableSpecies () {
		final List<Species> speciesList = getTransformableSpecies();
		speciesList.removeIf(species -> {
			final Family family = species.getFamily();

			// Remove the species if it doesn't have seeds, or if it's not the common species and its seed is the same as the common species'.
			return !species.hasSeed() || (species != family.getCommonSpecies() && species.getSeed() == family.getCommonSpecies().getSeed());
		});
		return speciesList;
	}

	//////////////////////////////
	// SAPLING HANDLING
	//////////////////////////////

	public final static Map<BlockState, Species> SAPLING_REPLACERS = new HashMap<>();

	public static void registerSaplingReplacer(BlockState state, Species species) {
		SAPLING_REPLACERS.put(state, species);
	}


	//////////////////////////////
	// DROP HANDLING
	//////////////////////////////

	public static final ResourceLocation GLOBAL = DynamicTrees.resLoc("global");

	/**
	 * This exists so that mods not interested in making Dynamic Trees can still add drops to
	 * all trees.
	 *
	 * @param dropCreator The {@link IDropCreator} to register.
	 */
	public static boolean registerDropCreator(@Nullable final ResourceLocation speciesName, final IDropCreator dropCreator) {
		if (speciesName == null || speciesName.equals(GLOBAL)) {
			return GLOBAL_DROP_CREATOR_STORAGE.addDropCreator(dropCreator);
		} else {
			return findSpecies(speciesName).addDropCreator(dropCreator);
		}
	}

	public static boolean registerGlobalDropCreator(final IDropCreator dropCreator){
		return registerDropCreator(GLOBAL, dropCreator);
	}

	public static boolean removeDropCreator(@Nullable final ResourceLocation speciesName, final ResourceLocation dropCreatorName) {
		if (speciesName == null || speciesName.equals(GLOBAL)) {
			return GLOBAL_DROP_CREATOR_STORAGE.remDropCreator(dropCreatorName);
		} else {
			return findSpecies(speciesName).remDropCreator(dropCreatorName);
		}
	}

	public static Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> getDropCreatorsMap() {
		final Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> dir = new HashMap<>();
		dir.put(GLOBAL, GLOBAL_DROP_CREATOR_STORAGE.getDropCreators());
		Species.REGISTRY.forEach(species -> dir.put(species.getRegistryName(), species.getDropCreators()));
		return dir;
	}

	//////////////////////////////
	// CELL KIT HANDLING
	//////////////////////////////

	@Nullable
	public static CellKit findCellKit(String name) {
		return findCellKit(getResLoc(name));
	}

	@Nullable
	public static CellKit findCellKit(ResourceLocation name) {
		return CellKit.REGISTRY.getValue(name);
	}

	//////////////////////////////
	// GROWTH LOGIC KIT HANDLING
	//////////////////////////////

	@Nullable
	public static GrowthLogicKit findGrowthLogicKit(String name) {
		return findGrowthLogicKit(getResLoc(name));
	}

	@Nullable
	public static GrowthLogicKit findGrowthLogicKit(final ResourceLocation name) {
		return GrowthLogicKit.REGISTRY.getValue(name);
	}

	public static ResourceLocation getResLoc (final String resLocStr) {
		return processResLoc(new ResourceLocation(resLocStr));
	}

	/**
	 * Changes namespace of resource location to "dynamictrees" as a default if it is set to Minecraft.
	 * This is safe since Minecraft won't (or shouldn't) have used any of our registries.
	 *
	 * @param resourceLocation The {@link ResourceLocation} to process.
	 * @return The {@link ResourceLocation} object.
	 */
	private static ResourceLocation processResLoc(final ResourceLocation resourceLocation) {
		if (DynamicTrees.MINECRAFT.equals(resourceLocation.getNamespace())) {
			return DynamicTrees.resLoc(resourceLocation.getPath());
		}
		return resourceLocation;
	}

}
