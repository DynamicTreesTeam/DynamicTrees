package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IDropCreator {
	
	ResourceLocation getName();
	
	/**
	 * Gets a list of drops for a {@link BlockDynamicLeaves} when the entire tree is harvested.
	 * NOT used for individual {@link BlockDynamicLeaves} being directly harvested by hand or tool. 
	 * 
	 * @param world
	 * @param species
	 * @param leafPos
	 * @param random
	 * @param dropList
	 * @param soilLife
	 * @param fortune
	 * @return
	 */
	List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune);
	
	/**
	 * Gets a {@link List} of voluntary drops.  Voluntary drops are {@link ItemStack}s that fall from the {@link DynamicTree} at
	 * random with no player interaction.
	 * 
	 * @param world
	 * @param species
	 * @param rootPos
	 * @param random
	 * @param dropList
	 * @param soilLife
	 * @return
	 */
	List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife);
	
	/**
	 * Gets a {@link List} of Leaves drops.  Leaves drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BlockDynamicLeaves} directly by hand or with a tool.
	 * 
	 * @param access
	 * @param species
	 * @param breakPos
	 * @param random
	 * @param dropList
	 * @param fortune
	 * @return
	 */
	List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune);

	/**
	 * Gets a {@link List} of Logs drops.  Logs drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BlockBranch} directly by hand or with a tool.
	 * 
	 * @param world
	 * @param species
	 * @param breakPos
	 * @param random
	 * @param dropList
	 * @param volume
	 * @return
	 */
	List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int volume);
	
}
