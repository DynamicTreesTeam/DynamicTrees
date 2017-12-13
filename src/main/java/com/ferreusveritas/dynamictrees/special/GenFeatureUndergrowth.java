package com.ferreusveritas.dynamictrees.special;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GenFeatureUndergrowth implements IGenFeature {

	private Species species;
	private int radius = 2;
	
	public GenFeatureUndergrowth(Species species) {
		this.species = species;
	}
	
	public GenFeatureUndergrowth setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {

		Vec3d vTree = new Vec3d(treePos).addVector(0.5, 0.5, 0.5);

		for(int i = 0; i < 2; i++) {

			int rad = MathHelper.clamp(radius, 2, world.rand.nextInt(radius - 1) + 2);
			Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (world.rand.nextFloat() * Math.PI * 2)));

			BlockPos pos = findGround(world, new BlockPos(v));
			IBlockState soilBlockState = world.getBlockState(pos);

			if(species.isAcceptableSoil(world, pos, soilBlockState)) {
					int type = world.rand.nextInt(2);
					world.setBlockState(pos, Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, type == 0 ? BlockPlanks.EnumType.OAK : BlockPlanks.EnumType.JUNGLE));
					pos = pos.up(world.rand.nextInt(3));
					
					SimpleVoxmap leafMap = species.getTree().getLeafCluster();
					for(BlockPos dPos : leafMap.getAllNonZero()) {
						BlockPos leafPos = pos.add(dPos);
						if(world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
							world.setBlockState(leafPos, Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, type == 0 ? BlockPlanks.EnumType.OAK : BlockPlanks.EnumType.JUNGLE));
						}
					}
			}
		}
	}
	
	BlockPos findGround(World world, BlockPos pos) {
				
		//Rise up until we are no longer in a solid block
		while(world.getBlockState(pos).isFullCube()) {
			pos = pos.up();
		}
		
		//Dive down until we are again
		while(!world.getBlockState(pos).isFullCube() && pos.getY() > 50) {
			pos = pos.down();
		}

		return pos;
	}
	
	
}
