package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NullLogic extends GrowthLogicKit {

	public NullLogic() {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "null"));
	}

	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		return probMap;
	}
	
	@Override
	public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
		return newDir;
	}
	
	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		return signalEnergy;
	}
	
}
