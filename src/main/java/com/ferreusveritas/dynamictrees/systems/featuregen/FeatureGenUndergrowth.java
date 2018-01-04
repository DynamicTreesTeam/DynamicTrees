package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FeatureGenUndergrowth implements IGenFeature {

	private Species species;
	private int radius = 2;
	
	public FeatureGenUndergrowth(Species species) {
		this.species = species;
	}
	
	public FeatureGenUndergrowth setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {

		Vec3d vTree = new Vec3d(treePos).addVector(0.5, 0.5, 0.5);

		for(int i = 0; i < 2; i++) {

			int rad = MathHelper.clamp(radius, 2, world.rand.nextInt(radius - 1) + 2);
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
					
					SimpleVoxmap leafMap = species.getTree().getCellKit().getLeafCluster();
					for(BlockPos dPos : leafMap.getAllNonZero()) {
						BlockPos leafPos = pos.add(dPos);
						if((coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
							world.setBlockState(leafPos, leavesState);
						}
					}
			}
		}
	}

	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}
	
}
