package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGrowthLogicKit {
	
	int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int probMap[]);
	
	EnumFacing newDirectionSelected(Species species, EnumFacing newDir, GrowSignal signal);
	
	float getEnergy(World world, BlockPos pos, Species species, float signalEnergy);
	
}
