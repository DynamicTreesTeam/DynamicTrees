package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeAcacia extends DynamicTree {

	public TreeAcacia() {
		super(VanillaTreeData.EnumType.ACACIA);

		//Acacia Trees are short, very slowly growing trees
		setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);

		envFactor(Type.COLD, 0.25f);
		envFactor(Type.NETHER, 0.75f);
		envFactor(Type.WET, 0.75f);

		setCellSolution(new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
		setHydroSolution(new short[]{0x02F0, 0x0143, 0x0133, 0x01F0});
		setSmotherLeavesMax(2);//very thin canopy
	}

	@Override
	public int getBranchHydrationLevel(IBlockAccess blockAccess, BlockPos pos, EnumFacing dir, BlockBranch branch, BlockDynamicLeaves fromBlock, int fromSub) {
		if(branch.getRadius(blockAccess, pos) == 1 && isCompatibleGrowingLeaves(fromBlock, fromSub)) {//Only compatible leaves
			if(dir == EnumFacing.DOWN) {
				return 3;
			} else
			if(dir != EnumFacing.UP) {//Disallow hydration from above.
				return 5;
			}
		}

		return 0;
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA);
	}

	@Override
	public void createLeafCluster(){

		setLeafCluster(new SimpleVoxmap(7, 2, 7, new byte[] {

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

		}).setCenter(new BlockPos(3, 0, 3)));

	}

}
