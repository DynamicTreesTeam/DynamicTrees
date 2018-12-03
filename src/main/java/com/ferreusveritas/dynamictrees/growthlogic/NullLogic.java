package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NullLogic implements IGrowthLogicKit {
	
	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		return probMap;
	}
	
	@Override
	public EnumFacing newDirectionSelected(Species species, EnumFacing newDir, GrowSignal signal) {
		return newDir;
	}
	
	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		return signalEnergy;
	}
	
}
