package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This works somewhat like a loot table except much more powerful.
 * 
 * @author ferreusveritas
 *
 */
public class DropCreatorStorage {
	
	private ArrayList<IDropCreator> dropCreators = new ArrayList<IDropCreator>();
	
	public void addDropCreator(IDropCreator dropCreator) {
		dropCreators.add(dropCreator);
	}
	
	private List<ItemStack> makeDropListIfNull(List<ItemStack> dropList) {
		if(dropList == null) {
			dropList = new ArrayList<ItemStack>();
		}
		return dropList;
	}
	
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators) {
			ItemStack stack = dropCreator.getHarvestDrop(world, species, leafPos, random, soilLife, fortune);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators) {
			ItemStack stack = dropCreator.getVoluntaryDrop(world, species, rootPos, random, soilLife);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		dropList = makeDropListIfNull(dropList);
		
		for(IDropCreator dropCreator : dropCreators) {
			ItemStack stack = dropCreator.getLeavesDrop(access, species, breakPos, random, fortune);
			if(!CompatHelper.isStackEmpty(stack)) {
				dropList.add(stack);
			}
		}
		
		return dropList;
	}
	
}
