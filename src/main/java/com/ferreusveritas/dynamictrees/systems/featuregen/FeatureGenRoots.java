package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureGenRoots implements IGenFeature {

	private Species species;
	private int radius = 2;
	private int trunkRadius = 0;
	private SimpleVoxmap rootMaps[];
	
	public FeatureGenRoots(Species species) {
		this.species = species;
		rootMaps = createRootMaps();
	}
	
	public SimpleVoxmap[] createRootMaps() {
		
		return new SimpleVoxmap[] { 
			
			new SimpleVoxmap(5, 1, 5, new byte[] {
					2, 3, 4, 0, 2,
					0, 0, 5, 0, 3,
					4, 5, 0, 5, 4,
					0, 0, 5, 0, 0,
					2, 3, 4, 0, 0,
			}).setCenter(new BlockPos(2, 0, 2)),

			new SimpleVoxmap(5, 1, 5, new byte[] {
					2, 3, 0, 0, 0,
					0, 4, 5, 0, 0,
					0, 0, 0, 5, 4,
					0, 0, 5, 0, 3,
					2, 3, 4, 0, 0,
			}).setCenter(new BlockPos(2, 0, 2)),

			
			new SimpleVoxmap(7, 1, 7, new byte[] {
					0, 2, 0, 0, 0, 0, 0,
					0, 3, 5, 0, 0, 0, 0,
					0, 0, 6, 0, 0, 2, 0,
					0, 0, 6, 0, 6, 4, 0,
					0, 0, 0, 6, 0, 3, 2,
					0, 0, 4, 5, 2, 0, 0,
					0, 2, 3, 0, 0, 0, 0,
			}).setCenter(new BlockPos(3, 0, 3)),
			
		};
	}
	
	public FeatureGenRoots setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	public FeatureGenRoots setTrunkRadius(int trunkRadius) {
		this.trunkRadius = trunkRadius;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {

		SimpleVoxmap rootMap = rootMaps[world.rand.nextInt(rootMaps.length)];
		if(trunkRadius > 13) {
			nextRoot(world, rootMap, treePos, BlockPos.ORIGIN, 0, null, 0);
		}
	}
	
	
	protected int getRootRadius(int trunkRadius) {
		if(trunkRadius > 13) {
			switch(trunkRadius) {
				case 14: return 3;
				case 15: return 4;
				case 16: return 5;
				case 17: return 6;
				default: return 8;
			}
		}
		return 0;
	}
	
	
	protected void nextRoot(World world, SimpleVoxmap rootMap, BlockPos trunkPos, BlockPos pos, int height, EnumFacing fromDir, int radius) {
		
		for(int i = 0; i < 2; i++) {
			BlockPos currPos = trunkPos.add(pos).up(height - i);
			IBlockState placeState = world.getBlockState(currPos);
			IBlockState belowState = world.getBlockState(currPos.down());
			
			if(pos == BlockPos.ORIGIN || (placeState.getBlock() == ModBlocks.blockTrunkShell || placeState.getBlock().isReplaceable(world, currPos)) && belowState.isNormalCube()) {
				if(radius > 0) {
					species.getFamily().getSurfaceRoots().setRadius(world, currPos, radius, fromDir, 3);
				}
				for(EnumFacing dir: EnumFacing.HORIZONTALS) {
					if(dir != fromDir) {
						BlockPos dPos = pos.offset(dir);
						byte rad = rootMap.getVoxel(dPos);
						if(rad != 0) {
							nextRoot(world, rootMap, trunkPos, dPos, height - i, dir.getOpposite(), rad);
						}
					}
				}
				break;
			}
		}
		
	}
	
}
