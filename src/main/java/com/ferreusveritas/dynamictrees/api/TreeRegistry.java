package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.dropcreators.StorageDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
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
	private static HashMap<ResourceLocation, ICellKit> CELL_KIT_REGISTRY = new HashMap<>();

	//////////////////////////////
	// SPECIES REGISTRY
	//////////////////////////////

	public static Species findSpecies(final String name) {
		return findSpecies(new ResourceLocation(name));
	}

	public static Species findSpecies(final ResourceLocation name) {
		final Species species = Species.REGISTRY.getValue(name);
		return species == null ? Species.NULL_SPECIES : species;
	}

	/**
	 * Searches first for the full tree name.  If that fails then it
	 * will find the first tree matching the simple name and return it instead otherwise null
	 *
	 * @param name The name of the tree.  Either the simple name or the full name
	 * @return The tree that was found or null if not found
	 */
	public static Species findSpeciesSloppy(final String name) {

		ResourceLocation resourceLocation = new ResourceLocation(name);
		if(DynamicTrees.MINECRAFT.equals(resourceLocation.getNamespace())) {//Minecraft(Mojang) isn't likely to have registered any Dynamic Tree species.
			resourceLocation = new ResourceLocation(DynamicTrees.MOD_ID, resourceLocation.getPath());//Search DynamicTrees Domain instead
		}

		//Search specific domain first
		if (Species.REGISTRY.containsKey(resourceLocation)) {
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
		return new ArrayList<>(Species.REGISTRY.getKeys());
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
			TreeFamily family = species.getFamily();

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

	public static final ResourceLocation globalName = new ResourceLocation(DynamicTrees.MOD_ID, "global");

	/**
	 * This exists so that mods not interested in making Dynamic Trees can still add drops to
	 * all trees.
	 *
	 * @param dropCreator The {@link IDropCreator} to register.
	 */
	public static boolean registerDropCreator(@Nullable final ResourceLocation speciesName, final IDropCreator dropCreator) {
		if(speciesName == null || speciesName.equals(globalName)) {
			return GLOBAL_DROP_CREATOR_STORAGE.addDropCreator(dropCreator);
		} else {
			return findSpecies(speciesName).addDropCreator(dropCreator);
		}
	}

	public static boolean registerGlobalDropCreator(final IDropCreator dropCreator){
		return registerDropCreator(globalName, dropCreator);
	}

	public static boolean removeDropCreator(@Nullable final ResourceLocation speciesName, ResourceLocation dropCreatorName) {
		if(speciesName == null || speciesName.equals(globalName)) {
			return GLOBAL_DROP_CREATOR_STORAGE.remDropCreator(dropCreatorName);
		} else {
			return findSpecies(speciesName).remDropCreator(dropCreatorName);
		}
	}

	public static Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> getDropCreatorsMap() {
		Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> dir = new HashMap<>();
		dir.put(globalName, GLOBAL_DROP_CREATOR_STORAGE.getDropCreators());
		Species.REGISTRY.forEach(species -> dir.put(species.getRegistryName(), species.getDropCreators()));
		return dir;
	}

	//////////////////////////////
	// CELLKIT HANDLING
	//////////////////////////////

	public static ICellKit registerCellKit(ResourceLocation name, ICellKit kit) {
		return CELL_KIT_REGISTRY.computeIfAbsent(name, k -> kit);
	}

	public static ICellKit findCellKit(ResourceLocation name) {
		return CELL_KIT_REGISTRY.get(name);
	}

	public static ICellKit findCellKit(String name) {
		ResourceLocation kitLocation = new ResourceLocation(name);
		if(DynamicTrees.MINECRAFT.equals(kitLocation.getNamespace())) {//Minecraft doesn't register leaves properties
			kitLocation = new ResourceLocation(DynamicTrees.MOD_ID, kitLocation.getPath());//Default to "dynamictrees" instead
		}
		return findCellKit(kitLocation);
	}

	public static void cleanupCellKit() {
		CELL_KIT_REGISTRY = new HashMap<>();
	}

	//////////////////////////////
	// GROWTHLOGICKIT HANDLING
	//////////////////////////////

	@Nullable
	public static GrowthLogicKit findGrowthLogicKit(final ResourceLocation name) {
		return GrowthLogicKit.REGISTRY.getValue(name);
	}

	@Nullable
	public static GrowthLogicKit findGrowthLogicKit(String name) {
		ResourceLocation kitLocation = new ResourceLocation(name);
		if("minecraft".equals(kitLocation.getNamespace())) {//Minecraft doesn't register leaves properties
			kitLocation = new ResourceLocation(DynamicTrees.MOD_ID, kitLocation.getPath());//Default to "dynamictrees" instead
		}
		return findGrowthLogicKit(kitLocation);
	}

}
