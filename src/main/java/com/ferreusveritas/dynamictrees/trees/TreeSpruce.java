package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.blocks.GrowSignal;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.Vec3d;

import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeSpruce extends DynamicTree {

	public TreeSpruce(int seq) {
		super("spruce", seq);

		//Spruce conical thick slower growing trees
		setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);

		setPrimitiveLeaves(Blocks.leaves, 1);
		setPrimitiveLog(Blocks.log, 1);
		setPrimitiveSapling(Blocks.sapling, 1);

		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.25f);
		envFactor(Type.WET, 0.75f);

		cellSolution = TreeHelper.cellSolverConifer;
		hydroSolution = TreeHelper.hydroSolverConifer;
		smotherLeavesMax = 2;

		registerBottomSpecials(new BottomListenerPodzol());
	}

	@Override
	protected int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]) {

		ForgeDirection originDir = signal.dir.getOpposite();
		
		//Alter probability map for direction change
		probMap[0] = 0;//Down is always disallowed for spruce
		probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = //Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
				!signal.isInTrunk() || (signal.isInTrunk() && signal.numSteps % 2 == 1 && radius > 1) ? 2 : 0;
		probMap[originDir.ordinal()] = 0;//Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 

		return probMap;
	}

	@Override
	protected ForgeDirection newDirectionSelected(ForgeDirection newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != ForgeDirection.UP){//Turned out of trunk
			signal.energy /= 3.0f;
		}
		return newDir;
	}

	@Override
	public int getBranchHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockBranch branch, BlockGrowingLeaves fromBlock, int fromSub) {
		if(branch.getRadius(blockAccess, x, y, z) == 1 && isCompatibleGrowingLeaves(fromBlock, fromSub)) {
			if(dir == ForgeDirection.DOWN && blockAccess.getBlock(x, y - 1, z) == branch) {
				return 5;
			}
			return (dir == ForgeDirection.UP || dir ==  ForgeDirection.DOWN) ? 2 : 3;
		}
		return 0;
	}

	//Spruce trees are so similar that it makes sense to randomize their height for a little variation
	//but we don't want the trees to always be the same height all the time when planted in the same location
	//so we feed the hash function the in-game month
	@Override
	public float getEnergy(World world, int x, int y, int z) {
		long day = world.getTotalWorldTime() / 24000L;
		int month = (int)day / 30;//Change the hashs every in-game month
		
		return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z) + (coordHashCode(x, y + month, z) % 5);//Vary the height energy by a psuedorandom hash function
	}

	public static int coordHashCode(int x, int y, int z) {
		int hash = (x * 9973 ^ y * 8287 ^ z * 9721) >> 1;
		return hash & 0xFFFF;
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.CONIFEROUS);
	}

	@Override
	public void createLeafCluster() {

		leafCluster = new SimpleVoxmap(5, 2, 5, new byte[] {

				//Layer 0(Bottom)
				0, 0, 1, 0, 0,
				0, 1, 2, 1, 0,
				1, 2, 0, 2, 1,
				0, 1, 2, 1, 0,
				0, 0, 1, 0, 0,

				//Layer 1 (Top)
				0, 0, 0, 0, 0,
				0, 0, 1, 0, 0,
				0, 1, 1, 1, 0,
				0, 0, 1, 0, 0,
				0, 0, 0, 0, 0

		}).setCenter(new Vec3d(2, 0, 2));

	}
}
