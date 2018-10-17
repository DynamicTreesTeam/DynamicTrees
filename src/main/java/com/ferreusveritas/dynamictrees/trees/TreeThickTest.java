package com.ferreusveritas.dynamictrees.trees;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TreeThickTest extends TreeFamily {
	
	public TreeThickTest() {
		super(new ResourceLocation(ModConstants.MODID, "test"));

		setPrimitiveLog(Blocks.BOOKSHELF.getDefaultState());
		
		ModBlocks.testLeavesProperties.setTree(this);
	}
	
	@Override
	public void createSpecies() {
		Species species = new Species(this.getName(), this, ModBlocks.testLeavesProperties) {
			{
				setBasicGrowingParameters(0.3f, 24.0f, 4, 4, 1.0f);
				setSoilLongevity(16); // Grows for a long long time

				setupStandardSeedDropping();
				setDynamicSapling(new BlockDynamicSapling("testsapling").getDefaultState());
				

				generateSeed();
			}
			
			@Override
			public boolean preGeneration(World world, BlockPos rootPos, int radius, EnumFacing facing, SafeChunkBounds safeBounds, JoCode joCode, IBlockState initialDirtState) {
				//Erase a volume of blocks that could potentially get in the way
				for(MutableBlockPos pos : BlockPos.getAllInBoxMutable(rootPos.add(new Vec3i(-1,  1, -1)), rootPos.add(new Vec3i(1, 6, 1)))) {
					world.setBlockToAir(pos);
				}
				return true;
			}
			
			@Override
			public void postGenerationDirtRepair(World world, BlockPos rootPos, IBlockState initialDirtState) {
				//Place dirt blocks around rooty dirt block if tree has a > 8 radius
				IBlockState branchState = world.getBlockState(rootPos.up());
				if(TreeHelper.getTreePart(branchState).getRadius(branchState) > BlockBranch.RADMAX_NORMAL) {
					for(Surround dir: Surround.values()) {
						world.setBlockState(rootPos.add(dir.getOffset()), initialDirtState);
					}
				}
			}
			
		};
		
		setCommonSpecies(species);
	}
	
	@Override
	public boolean isThick() {
		return true;
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		super.getRegisterableBlocks(blockList);
		blockList.add(getCommonSpecies().getDynamicSapling().getBlock());
		return blockList;
	}
	
}
