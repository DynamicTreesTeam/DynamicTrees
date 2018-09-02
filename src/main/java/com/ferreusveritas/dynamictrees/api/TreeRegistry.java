package com.ferreusveritas.dynamictrees.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorStorage;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

/**
* A registry for all of the dynamic trees. Use this for this mod or other mods.
* 
* @author ferreusveritas
*/
public class TreeRegistry {

	public static final IDropCreatorStorage globalDropCreatorStorage = new DropCreatorStorage();
	private static HashMap<ResourceLocation, ICellKit> cellKitRegistry = new HashMap<>(); 
	
	//////////////////////////////
	// SPECIES REGISTRY
	//////////////////////////////
	
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
		
		//Exact find
		ResourceLocation resloc = new ResourceLocation(name);
		if(Species.REGISTRY.containsKey(resloc)) {
			return Species.REGISTRY.getValue(resloc);
		}

		//Search DynamicTrees Domain
		resloc = new ResourceLocation(ModConstants.MODID, resloc.getResourcePath());
		if(Species.REGISTRY.containsKey(resloc)) {
			return Species.REGISTRY.getValue(resloc);
		}
		
		//Search all domains
		for(Species species : Species.REGISTRY) {
			if(species.getRegistryName().getResourcePath().equals(resloc.getResourcePath())) {
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
	
	public static Map<IBlockState, Species> saplingReplacers = new HashMap<>();
	
	public static void registerSaplingReplacer(IBlockState state, Species species) {
		saplingReplacers.put(state, species);
	}
	
	
	//////////////////////////////
	// DROP HANDLING
	//////////////////////////////
	
	public static final ResourceLocation globalName = new ResourceLocation(ModConstants.MODID, "global");
	
	/**
	 * This exists so that mods not interested in making Dynamic Trees can still add drops to
	 * all trees.
	 * 
	 * @param dropCreator
	 */
	public static boolean registerDropCreator(ResourceLocation speciesName, IDropCreator dropCreator) {
		if(speciesName == null || speciesName.equals(globalName)) {
			return globalDropCreatorStorage.addDropCreator(dropCreator);
		} else {
			return findSpecies(speciesName).addDropCreator(dropCreator);
		}
	}
	
	public static boolean removeDropCreator(ResourceLocation speciesName, ResourceLocation dropCreatorName) {
		if(speciesName == null || speciesName.equals(globalName)) {
			return globalDropCreatorStorage.remDropCreator(dropCreatorName);
		} else {
			return findSpecies(speciesName).remDropCreator(dropCreatorName);
		}
	}
	
	public static Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> getDropCreatorsMap() {
		Map<ResourceLocation, Map<ResourceLocation, IDropCreator>> dir = new HashMap<ResourceLocation, Map<ResourceLocation, IDropCreator>>();
		dir.put(globalName, globalDropCreatorStorage.getDropCreators());
		Species.REGISTRY.forEach(species -> dir.put(species.getRegistryName(), species.getDropCreators()));
		return dir;
	}
	
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
		return findCellKit(new ResourceLocation(ModConstants.MODID, name));
	}
	
}
