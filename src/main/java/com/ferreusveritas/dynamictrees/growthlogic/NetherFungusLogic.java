package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetherFungusLogic extends GrowthLogicKit {

	private static final int minCapHeight = 3;
	private static final int minCapHeightMega = 6;
	private static final int heightVariation = 8;

	public NetherFungusLogic(final ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
		if (signal.isInTrunk()){
			if (TreeHelper.isBranch(world.getBlockState(pos.above())) && !TreeHelper.isBranch(world.getBlockState(pos.above(3))))
				probMap = new int[]{0,0,0,0,0,0};
			else if (!species.isMegaSpecies())
				for (Direction direction : CoordUtils.HORIZONTALS)
					if (TreeHelper.isBranch(world.getBlockState(pos.offset(direction.getOpposite().getNormal()))))
						probMap[direction.get3DDataValue()] = 0;
			probMap[Direction.UP.get3DDataValue()] = 4;
		} else {
			probMap[Direction.UP.get3DDataValue()] = 0;
		}
		return probMap;
	}

	@Override
	public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != Direction.UP){//Turned out of trunk
				signal.energy = Math.min(signal.energy, species.isMegaSpecies()? 3 : 2);
		}
		return newDir;
	}

	private int getMinCapHeight (Species species){
		if (species.isMegaSpecies()) return minCapHeightMega;
		return minCapHeight;
	}

	private float getHashedVariation (World world, BlockPos pos){
		long day = world.getGameTime() / 24000L;
		int month = (int)day / 30;//Change the hashs every in-game month

		return (CoordUtils.coordHashCode(pos.above(month), 2) % heightVariation);//Vary the height energy by a psuedorandom hash function

	}

	@Override
	public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
		return Math.min(getLowestBranchHeight(world, pos, species, species.getLowestBranchHeight()) + getMinCapHeight(species) + getHashedVariation(world, pos)/1.5f, signalEnergy);
	}

	@Override
	public int getLowestBranchHeight(World world, BlockPos pos, Species species, int lowestBranchHeight) {
		return (int)(lowestBranchHeight * species.biomeSuitability(world, pos) + getHashedVariation(world, pos));//Vary the lowest branch height by a psuedorandom hash function
	}
}
