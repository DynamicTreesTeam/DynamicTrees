package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IDropCreator {
	
	ResourceLocation getName();
	
	ItemStack getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, int soilLife, int fortune);
	
	ItemStack getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, int soilLife);
	
	ItemStack getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, int fortune);
	
}
