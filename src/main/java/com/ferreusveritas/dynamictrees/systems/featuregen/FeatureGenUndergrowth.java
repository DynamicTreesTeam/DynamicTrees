package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenUndergrowth implements IPostGenFeature {

	private Species species;
	
	public FeatureGenUndergrowth(Species species) {
		this.species = species;
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		if(safeBounds != SafeChunkBounds.ANY && radius > 2) {//worldgen
			
			Vec3d vTree = new Vec3d(rootPos).addVector(0.5, 0.5, 0.5);
			
			for(int i = 0; i < 2; i++) {
				
				int rad = MathHelper.clamp(world.rand.nextInt(radius - 2) + 2, 2, radius - 1);
				Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (world.rand.nextFloat() * Math.PI * 2)));
				
				BlockPos pos = CoordUtils.findGround(world, new BlockPos(v));
				IBlockState soilBlockState = world.getBlockState(pos);
				
				if(species.isAcceptableSoil(world, pos, soilBlockState)) {
					int type = world.rand.nextInt(2);
					world.setBlockState(pos, Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, type == 0 ? BlockPlanks.EnumType.OAK : BlockPlanks.EnumType.JUNGLE));
					pos = pos.up(world.rand.nextInt(3));
					
					IBlockState leavesState = Blocks.LEAVES.getDefaultState()
							.withProperty(BlockOldLeaf.VARIANT, type == 0 ? BlockPlanks.EnumType.OAK : BlockPlanks.EnumType.JUNGLE)
							.withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));
					
					SimpleVoxmap leafMap = species.getLeavesProperties().getCellKit().getLeafCluster();
					MutableBlockPos leafPos = new MutableBlockPos();
					for(MutableBlockPos dPos : leafMap.getAllNonZero()) {
						leafPos.setPos(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
						if(safeBounds.inBounds(leafPos, false) && (CoordUtils.coordHashCode(leafPos, 0) % 5) != 0 && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
							world.setBlockState(leafPos, leavesState);
						}
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
	
}
