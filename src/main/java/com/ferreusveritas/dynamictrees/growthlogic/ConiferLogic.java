package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConiferLogic implements IGrowthLogicKit {
	
	private final float energyDivisor;
	private float horizontalLimiter = 16.0f;
	private int heightVariation = 5;
	
	public ConiferLogic() {
		this(3.0f);//Default for normal spruce
	}
	
	/**
	 * @param energyDivisor The remaining energy a branch has will be determined by this divisor.  Affects the conical slope of the tree shape.
	 */
	public ConiferLogic(float energyDivisor) {
		this.energyDivisor = energyDivisor;
	}
	
	/**
	 * Sets the maximum amount of energy a branch has left when leaving the trunk.  Helps to make a tree a more cylindrical shape.
	 * 
	 * @param energyLimiter
	 * @return ConiferLogic for chaining
	 */
	public ConiferLogic setHorizontalLimiter(float energyLimiter) {
		this.horizontalLimiter = energyLimiter;
		return this;
	}
	
	/**
	 * Sets the amount of psuedorandom height variation added to a tree.  Helpful to prevent all trees
	 * from turning out the same height.
	 * 
	 * @param heightVariation
	 * @return ConiferLogic for chaining
	 */
	public ConiferLogic setHeightVariation(int heightVariation) {
		this.heightVariation = heightVariation;
		return this;
	}
	
	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		
		EnumFacing originDir = signal.dir.getOpposite();
		
		//Alter probability map for direction change
		probMap[0] = 0;//Down is always disallowed for spruce
		probMap[1] = signal.isInTrunk() ? species.getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = //Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
				!signal.isInTrunk() || (signal.isInTrunk() && signal.numSteps % 2 == 1 && radius > 1) ? 2 : 0;
		probMap[originDir.ordinal()] = 0;//Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 
		
		return probMap;
	}
	
	@Override
	public EnumFacing newDirectionSelected(Species species, EnumFacing newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != EnumFacing.UP){//Turned out of trunk
			signal.energy /= energyDivisor;
			if (signal.energy > horizontalLimiter) signal.energy = horizontalLimiter;
		}
		return newDir;
	}
	
	//Spruce trees are so similar that it makes sense to randomize their height for a little variation
	//but we don't want the trees to always be the same height all the time when planted in the same location
	//so we feed the hash function the in-game month
	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		long day = world.getTotalWorldTime() / 24000L;
		int month = (int)day / 30;//Change the hashs every in-game month
		
		return signalEnergy * species.biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % heightVariation);//Vary the height energy by a psuedorandom hash function
	}
	
}
