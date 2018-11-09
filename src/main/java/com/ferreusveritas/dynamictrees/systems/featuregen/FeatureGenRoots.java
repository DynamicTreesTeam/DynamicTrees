package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureGenRoots implements IGenFeature {
	
	private Species species;
	private int radius = 2;
	private int levelLimit = 2;
	private Function<Integer, Integer> scaler = i -> i;
	
	private SimpleVoxmap rootMaps[];
	
	public FeatureGenRoots(Species species) {
		this.species = species;
		rootMaps = createRootMaps();
	}
	
	public SimpleVoxmap[] createRootMaps() {
		
		return new SimpleVoxmap[] { 
			
			new SimpleVoxmap(5, 1, 5, new byte[] {
					5, 6, 7, 0, 3,
					0, 0, 8, 0, 5,
					6, 8, 0, 8, 7,
					0, 0, 7, 0, 0,
					4, 5, 6, 0, 0,
			}).setCenter(new BlockPos(2, 0, 2)),
			
			new SimpleVoxmap(5, 1, 5, new byte[] {
					5, 6, 0, 0, 0,
					0, 7, 8, 0, 0,
					0, 0, 0, 7, 6,
					0, 0, 8, 0, 5,
					5, 6, 7, 0, 0,
			}).setCenter(new BlockPos(2, 0, 2)),
			
			new SimpleVoxmap(7, 1, 7, new byte[] {
					0, 4, 0, 0, 0, 0, 0,
					0, 5, 6, 0, 0, 0, 0,
					0, 0, 7, 0, 0, 3, 0,
					0, 0, 8, 0, 8, 7, 0,
					0, 0, 0, 8, 0, 5, 4,
					0, 0, 6, 7, 3, 0, 0,
					0, 4, 5, 0, 0, 0, 0,
			}).setCenter(new BlockPos(3, 0, 3)),
			
		};
	}
	
	public FeatureGenRoots setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	public FeatureGenRoots setLevelLimit(int limit) {
		this.levelLimit = limit;
		return this;
	}
	
	public FeatureGenRoots setScaler(Function<Integer, Integer> scaler) {
		this.scaler = scaler;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {		
		if(scaler != null) {
			SimpleVoxmap rootMap = rootMaps[world.rand.nextInt(rootMaps.length)];
			nextRoot(world, rootMap, treePos, BlockPos.ORIGIN, 0, -1, null, 0);
		}
	}
	
	protected void nextRoot(World world, SimpleVoxmap rootMap, BlockPos trunkPos, BlockPos pos, int height, int levelCount, EnumFacing fromDir, int radius) {
		
		for(int i = 0; i < 2; i++) {
			BlockPos currPos = trunkPos.add(pos).up(height - i);
			IBlockState placeState = world.getBlockState(currPos);
			IBlockState belowState = world.getBlockState(currPos.down());
			
			boolean onNormalCube = belowState.isNormalCube();
			
			if(pos == BlockPos.ORIGIN || isReplaceableWithRoots(world, placeState, currPos) && (i == 1 || onNormalCube)) {
				if(radius > 0) {
					species.getFamily().getSurfaceRoots().setRadius(world, currPos, radius, fromDir, 3);
				}
				if(onNormalCube) {
					for(EnumFacing dir: EnumFacing.HORIZONTALS) {
						if(dir != fromDir) {
							BlockPos dPos = pos.offset(dir);
							int nextRad = scaler.apply((int) rootMap.getVoxel(dPos));
							if(pos != BlockPos.ORIGIN && nextRad >= radius) {
								nextRad = radius - 1;
							}
							int thisLevelCount = i == 1 ? 1 : levelCount + 1;
							if(nextRad > 0 && thisLevelCount <= this.levelLimit) {//Don't go longer than 2 adjacent blocks on a single level
								nextRoot(world, rootMap, trunkPos, dPos, height - i, thisLevelCount, dir.getOpposite(), nextRad);
							}
						}
					}
				}
				break;
			}
		}
		
	}
	
	protected boolean isReplaceableWithRoots(World world, IBlockState placeState, BlockPos pos) {
		Block block = placeState.getBlock();
		if(block == Blocks.AIR || block == ModBlocks.blockTrunkShell) {
			return true;
		}
		
		Material material = placeState.getMaterial();
		
		return block.isReplaceable(world, pos) && material != Material.WATER && material != Material.LAVA;
	}
}
