package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This works somewhat like a loot table except much more powerful.
 * 
 * @author ferreusveritas
 *
 */
public class StorageDropCreator implements IDropCreatorStorage {
	
	private HashMap<ResourceLocation, IDropCreator> dropCreators = new HashMap<ResourceLocation, IDropCreator>();
	
	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(DynamicTrees.MODID, "storage");
	}
	
	@Override
	public boolean addDropCreator(IDropCreator dropCreator) {
		dropCreators.put(dropCreator.getName(), dropCreator);
		return true;
	}
	
	@Override
	public IDropCreator findDropCreator(ResourceLocation name) {
		return dropCreators.get(name);
	}
	
	@Override
	public boolean remDropCreator(ResourceLocation name) {
		return dropCreators.remove(name) != null;
	}
	
	@Override
	public Map<ResourceLocation, IDropCreator> getDropCreators() {
		return new HashMap<ResourceLocation, IDropCreator>(dropCreators);
	}
	
	private List<ItemStack> makeDropListIfNull(List<ItemStack> dropList) {
		if(dropList == null) {
			dropList = new ArrayList<ItemStack>();
		}
		return dropList;
	}
	
	@Override
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			dropList = dropCreator.getHarvestDrop(world, species, leafPos, random, dropList, soilLife, fortune);
		}
		
		return dropList;
	}
	
	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			dropList = dropCreator.getVoluntaryDrop(world, species, rootPos, random, dropList, soilLife);
		}
		
		return dropList;
	}
	
	@Override
	public List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			dropList = dropCreator.getLeavesDrop(access, species, breakPos, random, dropList, fortune);
		}
		
		return dropList;
	}
	
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, float volume) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			dropList = dropCreator.getLogsDrop(world, species, rootPos, random, dropList, volume);
		}
		
		return dropList;
	}
	
}
