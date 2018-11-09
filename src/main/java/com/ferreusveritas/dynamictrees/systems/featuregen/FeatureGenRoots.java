package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
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
	private int levelLimit = 2;
	private Function<Integer, Integer> scaler = i -> i;
	
	private SimpleVoxmap rootMaps[];
	
	public FeatureGenRoots(Species species) {
		this.species = species;
		rootMaps = createRootMaps();
	}
	
	protected SimpleVoxmap[] createRootMaps() {
		//These are basically bitmaps of the root structures
		byte[][] rootData = new byte[][] {
			{0,3,0,0,0,0,0,0,5,6,7,0,3,2,0,0,0,8,0,5,0,0,6,8,0,8,7,0,0,0,0,7,0,0,0,0,3,4,6,4,0,0,0,2,0,0,3,2,1},
			{0,3,0,0,0,0,0,0,5,6,7,0,3,2,0,0,0,8,0,5,0,0,6,8,0,8,7,0,0,0,0,7,0,0,0,0,3,4,6,4,0,0,0,2,0,0,3,2,1},
			{0,0,2,0,0,0,0,3,4,6,0,0,0,0,1,0,7,8,0,0,0,0,0,0,0,7,6,0,0,0,0,8,0,5,4,0,5,6,7,0,0,2,2,4,0,0,0,0,0},
			{0,4,0,0,0,0,0,0,5,6,0,0,1,0,0,0,7,0,0,3,0,0,0,8,0,8,7,0,0,0,0,8,0,5,4,0,0,6,7,3,0,2,0,4,5,0,0,0,0},
			{3,4,5,0,0,0,0,2,0,6,0,0,0,0,0,0,7,8,0,0,0,0,0,0,0,0,0,0,0,0,0,8,7,0,0,0,0,0,0,6,0,0,0,0,2,3,5,2,0},
			{0,0,4,0,0,0,0,0,0,6,7,0,2,0,0,0,0,8,0,3,0,5,7,8,0,6,5,0,3,0,0,8,0,2,1,0,3,0,7,0,0,0,0,4,5,6,0,0,0}
		};
		
		SimpleVoxmap maps[] = new SimpleVoxmap[rootData.length];
		
		for(int i = 0; i < maps.length; i++) {
			maps[i] = new SimpleVoxmap(7, 1, 7, rootData[i]).setCenter(new BlockPos(3, 0, 3));
		}
		
		return maps;
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
			int hash = CoordUtils.coordHashCode(treePos, 2);
			SimpleVoxmap rootMap = rootMaps[hash % rootMaps.length];
			nextRoot(world, rootMap, treePos, BlockPos.ORIGIN, 0, -1, null, 0);
		}
	}
	
	protected void nextRoot(World world, SimpleVoxmap rootMap, BlockPos trunkPos, BlockPos pos, int height, int levelCount, EnumFacing fromDir, int radius) {
		
		for(int depth = 0; depth < 2; depth++) {
			BlockPos currPos = trunkPos.add(pos).up(height - depth);
			IBlockState placeState = world.getBlockState(currPos);
			IBlockState belowState = world.getBlockState(currPos.down());
			
			boolean onNormalCube = belowState.isNormalCube();
			
			if(pos == BlockPos.ORIGIN || isReplaceableWithRoots(world, placeState, currPos) && (depth == 1 || onNormalCube)) {
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
							int thisLevelCount = depth == 1 ? 1 : levelCount + 1;
							if(nextRad > 0 && thisLevelCount <= this.levelLimit) {//Don't go longer than 2 adjacent blocks on a single level
								nextRoot(world, rootMap, trunkPos, dPos, height - depth, thisLevelCount, dir.getOpposite(), nextRad);//Recurse here
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
