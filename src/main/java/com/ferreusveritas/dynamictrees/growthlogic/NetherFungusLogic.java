package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetherFungusLogic extends GrowthLogicKit {

	public NetherFungusLogic(final ResourceLocation registryName) {
		super(registryName);
	}

	private boolean isNextToTrunk (BlockPos signalPos, GrowSignal signal){
		if (signal.numTurns != 1) return false;
		BlockPos rootPos = signal.rootPos;
		if (signalPos.getZ() < rootPos.getZ()){
			return  rootPos.getZ() - signalPos.getZ() == 1;
		} else if (signalPos.getZ() > rootPos.getZ()){
			return signalPos.getZ() - rootPos.getZ() == 1;
		}else if (signalPos.getX() > rootPos.getX()){
			return signalPos.getX() - rootPos.getX() == 1;
		}else if (signalPos.getX() < rootPos.getX()){
			return rootPos.getX() - signalPos.getX() == 1;
		}else {
			return false;
		}
	}

	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {

		//Disallow up/down turns after having turned out of the trunk once.
		if(!signal.isInTrunk()) {
			probMap[Direction.UP.get3DDataValue()] = 0;
			probMap[Direction.DOWN.get3DDataValue()] = 0;

			//Ensure that the branch gets out of the trunk at least two blocks so it won't interfere with new side branches at the same level
			if (isNextToTrunk(pos, signal)){
				signal.energy = 1;
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
		return signalEnergy;
	}
	
}
