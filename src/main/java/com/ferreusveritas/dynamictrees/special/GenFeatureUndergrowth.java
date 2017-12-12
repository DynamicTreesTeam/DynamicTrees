package com.ferreusveritas.dynamictrees.special;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
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

		Vec3d v = new Vec3d(treePos).addVector(0.5, 0.5, 0.5);
		v.add(new Vec3d(1, 0, 0).scale(radius).rotateYaw((float) (world.rand.nextFloat() * Math.PI * 2)));
		
		BlockPos pos = findGround(world, new BlockPos(v));
		IBlockState soilBlockState = world.getBlockState(pos);
		
		if(!TreeHelper.isRootyDirt(soilBlockState) && species.isAcceptableSoil(world, pos, soilBlockState)) {
			world.setBlockState(pos, species.getRootyDirtBlock().getDefaultState().withProperty(BlockRootyDirt.LIFE, 0));
			world.setBlockState(pos.up(1), species.getTree().getDynamicBranch().getDefaultState().withProperty(BlockBranch.RADIUS, 1));
			world.setBlockState(pos.up(2), species.getTree().getPrimitiveLeaves());
		}
		
	}

	BlockPos findGround(World world, BlockPos pos) {
				
		//Rise up until we are no longer in a solid block
		while(world.getBlockState(pos).isFullCube()) {
			pos = pos.up();
		}
		
		//Dive down until we are again
		while(!world.getBlockState(pos).isFullCube() || pos.getY() < 50) {
			pos = pos.down();
		}

		return pos;
	}
	
	
}
