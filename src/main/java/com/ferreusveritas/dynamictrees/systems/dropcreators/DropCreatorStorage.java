package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This works somewhat like a loot table except much more powerful.
 * 
 * @author ferreusveritas
 *
 */
public class DropCreatorStorage implements IDropCreatorStorage {
	
	private HashMap<ResourceLocation, IDropCreator> dropCreators = new HashMap<ResourceLocation, IDropCreator>();
	
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
		return new HashMap(dropCreators);
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
			ItemStack stack = dropCreator.getHarvestDrop(world, species, leafPos, random, soilLife, fortune);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			ItemStack stack = dropCreator.getVoluntaryDrop(world, species, rootPos, random, soilLife);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators.values()) {
			ItemStack stack = dropCreator.getLeavesDrop(access, species, breakPos, random, fortune);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
}
