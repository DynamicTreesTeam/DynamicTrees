package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DropCreatorVoluntary implements IDropCreator {

	ResourceLocation name;
	ItemStack droppedItem;
	float rate;
	
	public DropCreatorVoluntary(ResourceLocation name, ItemStack droppedItem, float rate) {
		this.name = name;
		this.droppedItem = droppedItem;
		this.rate = rate;
	}
	
	@Override
	public ResourceLocation getName() {
		return name;
	}

	@Override
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		return dropList;
	}

	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		if(rate > random.nextFloat()) {
			dropList.add(droppedItem.copy());
		}
		return dropList;
	}

	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		return dropList;
	}

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int volume) {
		return dropList;
	}
	
}
