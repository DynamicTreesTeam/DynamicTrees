package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DarkOakLogic implements IGrowthLogicKit {

	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		probMap[EnumFacing.UP.getIndex()] = 4;
		
		//Disallow up/down turns after having turned out of the trunk once.
		if(!signal.isInTrunk()) {
			probMap[EnumFacing.UP.getIndex()] = 0;
			probMap[EnumFacing.DOWN.getIndex()] = 0;
			probMap[signal.dir.ordinal()] *= 0.35;//Promotes the zag of the horizontal branches
		}
		
		//Amplify cardinal directions to encourage spread the higher we get
		float energyRatio = signal.delta.getY() / species.getEnergy(world, pos);
		float spreadPush = energyRatio * 2;
		spreadPush = spreadPush < 1.0f ? 1.0f : spreadPush;
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			probMap[dir.ordinal()] *= spreadPush;
		}
		
		//Ensure that the branch gets out of the trunk at least two blocks so it won't interfere with new side branches at the same level 
		if(signal.numTurns == 1 && signal.delta.distanceSq(0, signal.delta.getY(), 0) == 1.0 ) {
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				if(signal.dir != dir) {
					probMap[dir.ordinal()] = 0;
				}
			}
		}
		
		//If the side branches are too swole then give some other branches a chance
		if(signal.isInTrunk()) {
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				if(probMap[dir.ordinal()] >= 7) {
					probMap[dir.ordinal()] = 2;
				}
			}
			if(signal.delta.getY() > species.getLowestBranchHeight() + 5) {
				probMap[EnumFacing.UP.ordinal()] = 0;
				signal.energy = 2;
			}
		}
		
		return probMap;	}

	@Override
	public EnumFacing newDirectionSelected(Species species, EnumFacing newDir, GrowSignal signal) {
		return newDir;
	}

	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		return signalEnergy;
	}
	
}
