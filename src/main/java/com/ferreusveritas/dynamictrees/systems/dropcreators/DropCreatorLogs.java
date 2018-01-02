package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DropCreatorLogs implements IDropCreator {

	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "logs");
	}

	@Override
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		return dropList;
	}

	@Override
	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		return dropList;
	}

	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		return dropList;
	}

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int volume) {
		dropList.add(species.getTree().getPrimitiveLogItemStack(volume / 4096));// A log contains 4096 voxels of wood material(16x16x16 pixels) Drop vanilla logs or whatever
		dropList.add(species.getTree().getStick((volume % 4096) / 512));// A stick contains 512 voxels of wood (1/8th log) (1 log = 4 planks, 2 planks = 4 sticks) Give him the stick!
		return dropList;
	}

}
