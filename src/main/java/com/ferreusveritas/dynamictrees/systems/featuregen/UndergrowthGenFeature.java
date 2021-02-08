package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

public class UndergrowthGenFeature implements IPostGenFeature {

	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY && radius > 2) {//worldgen

			Vector3d vTree = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

			for(int i = 0; i < 2; i++) {

				int rad = MathHelper.clamp(world.getRandom().nextInt(radius - 2) + 2, 2, radius - 1);
				Vector3d v = vTree.add(new Vector3d(1, 0, 0).scale(rad).rotateYaw((float) (world.getRandom().nextFloat() * Math.PI * 2)));
				BlockPos vPos = new BlockPos(v);
				if (!safeBounds.inBounds(vPos, true)) continue;

				BlockPos pos = CoordUtils.findGround(world, vPos);
				BlockState soilBlockState = world.getBlockState(pos);

				if(species.isAcceptableSoil(world, pos, soilBlockState)) {
					int type = world.getRandom().nextInt(2);
					world.setBlockState(pos, (type == 0 ? Blocks.OAK_LOG : Blocks.JUNGLE_LOG).getDefaultState(), 2);
					pos = pos.up(world.getRandom().nextInt(3));

					BlockState leavesState = (type == 0 ? Blocks.OAK_LEAVES : Blocks.JUNGLE_LEAVES).getDefaultState().with(LeavesBlock.PERSISTENT, true);

					SimpleVoxmap leafMap = species.getLeavesProperties().getCellKit().getLeafCluster();
					BlockPos.Mutable leafPos = new BlockPos.Mutable();
					for(BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
						leafPos.setPos(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
						if(safeBounds.inBounds(leafPos, true) && (CoordUtils.coordHashCode(leafPos, 0) % 5) != 0 && world.getBlockState(leafPos).canBeReplacedByLeaves(world, leafPos)) {
							world.setBlockState(leafPos, leavesState, 2);
						}
					}
				}
			}

			return true;
		}

		return false;
	}

}
