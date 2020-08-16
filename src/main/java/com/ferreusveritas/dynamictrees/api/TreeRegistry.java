package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.growthlogic.IGrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorStorage;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

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

	public static final IDropCreatorStorage globalDropCreatorStorage = new DropCreatorStorage();
	private static HashMap<ResourceLocation, ICellKit> cellKitRegistry = new HashMap<>();
	private static HashMap<ResourceLocation, IGrowthLogicKit> growthLogicKitRegistry = new HashMap<>();

	//////////////////////////////
	// SPECIES REGISTRY
	//////////////////////////////

	public static Species findSpecies(String name) {
		return findSpecies(new ResourceLocation(name));
	}

	public static Species findSpecies(ResourceLocation name) {
		return Species.REGISTRY.getValue(name);
	}

	/**
	 * Searches first for the full tree name.  If that fails then it
	 * will find the first tree matching the simple name and return it instead otherwise null
	 *
	 * @param name The name of the tree.  Either the simple name or the full name
	 * @return The tree that was found or null if not found
	 */
	public static Species findSpeciesSloppy(String name) {

		ResourceLocation resloc = new ResourceLocation(name);
		if("minecraft".equals(resloc.getNamespace())) {//Minecraft(Mojang) isn't likely to have registered any Dynamic Tree species.
			resloc = new ResourceLocation(DynamicTrees.MODID, resloc.getPath());//Search DynamicTrees Domain instead
		}

		//Search specific domain first
		if(Species.REGISTRY.containsKey(resloc)) {
			return Species.REGISTRY.getValue(resloc);
		}

		//Search all domains
		for(Species species : Species.REGISTRY) {
			if(species.getRegistryName().getPath().equals(resloc.getPath())) {
				return species;
			}
		}

		return Species.NULLSPECIES;
	}

	public static List<ResourceLocation> getSpeciesDirectory() {
		return new ArrayList<ResourceLocation>(Species.REGISTRY.getKeys());
	}

	//////////////////////////////
	// SAPLING HANDLING
	//////////////////////////////

	public static Map<BlockState, Species> saplingReplacers = new HashMap<>();

	public static void registerSaplingReplacer(BlockState state, Species species) {
		saplingReplacers.put(state, species);
	}


//	//////////////////////////////
//	// DROP HANDLING
//	//////////////////////////////
//
//	public static final ResourceLocation globalName = new ResourceLocation(DynamicTrees.MODID, "global");
//
//	/**
//	 * This exists so that mods not interested in making Dynamic Trees can still add drops to
//	 * all trees.
//	 *
//	 * @param dropCreator
//	 */
//	public static boolean registerDropCreator(ResourceLocation speciesName, IDropCreator dropCreator) {
//		if(speciesName == null || speciesName.equals(globalName)) {
//			return globalDropCreatorStorage.addDropCreator(dropCreator);
//		} else {
//			return findSpecies(speciesName).addDropCreator(dropCreator);
//		}
//	}
//
//	public static boolean removeDropCreator(ResourceLocation speciesName, ResourceLocation dropCreatorName) {
//		if(speciesName == null || speciesName.equals(globalName)) {
//			return globalDropCreatorStorage.remDropCreator(dropCreatorName);
//		} else {
//			return findSpecies(speciesName).remDropCreator(dropCreatorName);
//		}
//	}
//
//	public static Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> getDropCreatorsMap() {
//		Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> dir = new HashMap<ResourceLocation, Map<ResourceLocation, IDropCreator>>();
//		dir.put(globalName, globalDropCreatorStorage.getDropCreators());
//		Species.REGISTRY.forEach(species -> dir.put(species.getRegistryName(), species.getDropCreators()));
//		return dir;
//	}

	//////////////////////////////
	// CELLKIT HANDLING
	//////////////////////////////

	public static ICellKit registerCellKit(ResourceLocation name, ICellKit kit) {
		return cellKitRegistry.computeIfAbsent(name, k -> kit);
	}

	public static ICellKit findCellKit(ResourceLocation name) {
		return cellKitRegistry.get(name);
	}

	public static ICellKit findCellKit(String name) {
		ResourceLocation kitLocation = new ResourceLocation(name);
		if("minecraft".equals(kitLocation.getNamespace())) {//Minecraft doesn't register leaves properties
			kitLocation = new ResourceLocation(DynamicTrees.MODID, kitLocation.getPath());//Default to "dynamictrees" instead
		}
		return findCellKit(kitLocation);
	}

	public static void cleanupCellKit() {
		cellKitRegistry = new HashMap<>();
	}

	//////////////////////////////
	// GROWTHLOGICKIT HANDLING
	//////////////////////////////

	public static IGrowthLogicKit registerGrowthLogicKit(ResourceLocation name, IGrowthLogicKit kit) {
		return growthLogicKitRegistry.computeIfAbsent(name, k -> kit);
	}

	public static IGrowthLogicKit findGrowthLogicKit(ResourceLocation name) {
		return growthLogicKitRegistry.get(name);
	}

	public static IGrowthLogicKit findGrowthLogicKit(String name) {
		ResourceLocation kitLocation = new ResourceLocation(name);
		if("minecraft".equals(kitLocation.getNamespace())) {//Minecraft doesn't register leaves properties
			kitLocation = new ResourceLocation(DynamicTrees.MODID, kitLocation.getPath());//Default to "dynamictrees" instead
		}
		return findGrowthLogicKit(kitLocation);
	}

	public static void cleanupGrowthLogicKit() {
		growthLogicKitRegistry = new HashMap<>();
	}

}
