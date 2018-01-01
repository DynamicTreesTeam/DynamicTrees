package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IDropCreatorStorage {

	public boolean addDropCreator(IDropCreator dropCreator);

	public IDropCreator findDropCreator(ResourceLocation name);
	
	public boolean remDropCreator(ResourceLocation name);
	
	public Map<ResourceLocation, IDropCreator> getDropCreators();
	
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune);
	
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife);
	
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune);
	
}
