package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenMound implements IPreGenFeature, IPostGenFeature {
	
	private static SimpleVoxmap moundMap = new SimpleVoxmap(5, 4, 5, new byte[] {
			0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0,
			0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0,
			0, 1, 1, 1, 0, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 0, 1, 1, 1, 0,
			0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0
		}).setCenter(new BlockPos(2, 3, 2));
	
	private final int moundCutoffRadius;
	
	public FeatureGenMound(Species species, int moundCutoffRadius) {
		this.moundCutoffRadius = moundCutoffRadius;
	}
	
	/**
	 * Used to create a 5x4x5 rounded mound that is one block higher than the ground surface.
	 * This is meant to replicate the appearance of a root hill and gives generated surface 
	 * roots a better appearance.
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt
	 * @param safeBounds A safebounds structure for preventing runaway cascading generation
	 * @return The modified position of the rooty dirt that is one block higher
	 */
	@Override
	public BlockPos preGeneration(World world, BlockPos rootPos, int radius, EnumFacing facing, SafeChunkBounds safeBounds, JoCode joCode) {
		if(radius >= moundCutoffRadius && safeBounds != SafeChunkBounds.ANY) {//worldgen test
			IBlockState initialDirtState = world.getBlockState(rootPos);
			IBlockState initialUnderState = world.getBlockState(rootPos.down());
			
			if(initialUnderState.getMaterial() != Material.GROUND || initialUnderState.getMaterial() != Material.ROCK) {
				initialUnderState = ModBlocks.blockStates.dirt;
			}
			
			rootPos = rootPos.up();
			
			for(Cell cell: moundMap.getAllNonZeroCells()) {
				IBlockState placeState = cell.getValue() == 1 ? initialDirtState : initialUnderState;
				world.setBlockState(rootPos.add(cell.getPos()), placeState);
			}
		}
		
		return rootPos;
	}
	
	/** 
	 * Creates a 3x2x3 cube of dirt around the base of the tree using blocks derived from the
	 * environment.  This is used to cleanup the overhanging trunk that happens when a thick
	 * tree is generated next to a drop off.  Only runs when the radius is greater than 8.
	 */
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		if(radius < moundCutoffRadius && safeBounds != SafeChunkBounds.ANY) {//A mound was already generated in preGen and worldgen test
			BlockPos treePos = rootPos.up();
			IBlockState belowState = world.getBlockState(rootPos.down());
			
			//Place dirt blocks around rooty dirt block if tree has a > 8 radius
			IBlockState branchState = world.getBlockState(treePos);
			if(TreeHelper.getTreePart(branchState).getRadius(branchState) > BlockBranch.RADMAX_NORMAL) {
				for(Surround dir: Surround.values()) {
					BlockPos dPos = rootPos.add(dir.getOffset());
					world.setBlockState(dPos, initialDirtState);
					world.setBlockState(dPos.down(), belowState);
				}
				return true;
			}
		}
		
		return false;
	}
	
}
