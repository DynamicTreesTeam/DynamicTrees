package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public interface IDropCreator {

	ResourceLocation getName();

	/**
	 * Gets a list of drops for a {@link DynamicLeavesBlock} when the entire tree is harvested.
	 * NOT used for individual {@link DynamicLeavesBlock} being directly harvested by hand or tool.
	 *
	 * @param world
	 * @param species
	 * @param leafPos
	 * @param random
	 * @param dropList
	 * @param fertility
	 * @param fortune
	 * @return
	 */
	List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int fertility, int fortune);

	/**
	 * Gets a {@link List} of voluntary drops.  Voluntary drops are {@link ItemStack}s that fall from the {@link Family} at
	 * random with no player interaction.
	 *
	 * @param world
	 * @param species
	 * @param rootPos
	 * @param random
	 * @param dropList
	 * @param fertility
	 * @return
	 */
	List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int fertility);

	/**
	 * Gets a {@link List} of Leaves drops.  Leaves drops are {@link ItemStack}s that result from the breaking of
	 * a {@link DynamicLeavesBlock} directly by hand or with a tool.
	 *
	 * @param access
	 * @param species
	 * @param breakPos
	 * @param random
	 * @param dropList
	 * @param fortune
	 * @return
	 */
	List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune);

	/**
	 * Gets a {@link List} of Logs drops.  Logs drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BranchBlock} directly by hand or with a tool.
	 *
	 * @param world
	 * @param species
	 * @param breakPos
	 * @param random
	 * @param dropList
	 * @param volume
	 * @return
	 */
	List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NetVolumeNode.Volume volume);

}
