package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DarkOakLogic extends GrowthLogicKit {

	public DarkOakLogic(final ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		probMap[Direction.UP.getIndex()] = 4;
		
		//Disallow up/down turns after having turned out of the trunk once.
		if(!signal.isInTrunk()) {
			probMap[Direction.UP.getIndex()] = 0;
			probMap[Direction.DOWN.getIndex()] = 0;
			probMap[signal.dir.ordinal()] *= 0.35;//Promotes the zag of the horizontal branches
		}
		
		//Amplify cardinal directions to encourage spread the higher we get
		float energyRatio = signal.delta.getY() / species.getEnergy(world, pos);
		float spreadPush = energyRatio * 2;
		spreadPush = Math.max(spreadPush, 1.0f);
		for(Direction dir: CoordUtils.HORIZONTALS) {
			probMap[dir.ordinal()] *= spreadPush;
		}
		
		//Ensure that the branch gets out of the trunk at least two blocks so it won't interfere with new side branches at the same level 
		if(signal.numTurns == 1 && signal.delta.distanceSq(0, signal.delta.getY(), 0, true) == 1.0 ) {
			for(Direction dir: CoordUtils.HORIZONTALS) {
				if(signal.dir != dir) {
					probMap[dir.ordinal()] = 0;
				}
			}
		}
		
		//If the side branches are too swole then give some other branches a chance
		if(signal.isInTrunk()) {
			for(Direction dir: CoordUtils.HORIZONTALS) {
				if(probMap[dir.ordinal()] >= 7) {
					probMap[dir.ordinal()] = 2;
				}
			}
			if(signal.delta.getY() > species.getLowestBranchHeight() + 5) {
				probMap[Direction.UP.ordinal()] = 0;
				signal.energy = 2;
			}
		}
		
		return probMap;
	}

	@Override
	public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
		return newDir;
	}

	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		return signalEnergy * species.biomeSuitability(world, pos);
	}
	
}
