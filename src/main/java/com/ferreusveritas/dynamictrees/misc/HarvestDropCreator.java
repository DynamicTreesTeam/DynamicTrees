package com.ferreusveritas.dynamictrees.misc;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HarvestDropCreator implements IDropCreator {

	ResourceLocation name;
	ItemStack droppedItem;
	float rate;
	
	public HarvestDropCreator(ResourceLocation name, ItemStack droppedItem, float rate) {
		this.name = name;
		this.droppedItem = droppedItem;
		this.rate = rate;
	}
	
	@Override
	public ResourceLocation getName() {
		return name;
	}

	@Override
	public ItemStack getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, int soilLife, int fortune) {
		return rate > random.nextFloat() ? droppedItem : CompatHelper.emptyStack();
	}

	@Override
	public ItemStack getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, int soilLife) {
		return CompatHelper.emptyStack();
	}

	@Override
	public ItemStack getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, int fortune) {
		return CompatHelper.emptyStack();
	}

}
