package com.ferreusveritas.growingtrees.trees;

import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.growingtrees.util.SimpleVoxmap;
import com.ferreusveritas.growingtrees.util.Vec3d;

import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeAcacia extends GrowingTree {

	public TreeAcacia(int seq) {
		super("acacia", seq);
		
		//Acacia Trees are short, very slowly growing trees
		setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);

		setPrimitiveLeaves(Blocks.leaves2, 0);//Vanilla Acacia leaves
		setPrimitiveLog(Blocks.log2, 0);
		setPrimitiveSapling(Blocks.sapling, 4);

        envFactor(Type.COLD, 0.25f);
        envFactor(Type.NETHER, 0.75f);
        envFactor(Type.WET, 0.75f);
		
		cellSolution = new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211};
		hydroSolution = new short[]{0x02F0, 0x0143, 0x0133, 0x01F0};
		smotherLeavesMax = 2;//very thin canopy
	}

	@Override
	public int getBranchHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockBranch branch, BlockGrowingLeaves fromBlock, int fromSub) {
		if(branch.getRadius(blockAccess, x, y, z) == 1 && isCompatibleGrowingLeaves(fromBlock, fromSub)) {//Only compatible leaves
			if(dir == ForgeDirection.DOWN) {
				return 3;
			} else
			if(dir != ForgeDirection.UP) {//Disallow hydration from above.
				return 5;
			}
		}

		return 0;
	}
	
	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA);
	}
	
	public void createLeafCluster(){

		leafCluster = new SimpleVoxmap(7, 2, 7, new byte[] {

				//Layer 0(Bottom)
				0, 0, 1, 1, 1, 0, 0,
				0, 1, 2, 2, 2, 1, 0,
				1, 2, 3, 4, 3, 2, 1,
				1, 2, 4, 0, 4, 2, 1,
				1, 2, 3, 4, 3, 2, 1,
				0, 1, 2, 2, 2, 1, 0,
				0, 0, 1, 1, 1, 0, 0,

				//Layer 1 (Top)
				0, 0, 0, 0, 0, 0, 0,
				0, 0, 1, 1, 1, 0, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 0, 1, 1, 1, 0, 0,
				0, 0, 0, 0, 0, 0, 0

		}).setCenter(new Vec3d(3, 0, 3));

	}
	
	
}
