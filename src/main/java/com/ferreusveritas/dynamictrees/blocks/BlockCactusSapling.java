package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCactusSapling extends BlockDynamicSapling {
	
	public BlockCactusSapling(String name) {
		super(name);
		setSoundType(SoundType.CLOTH);
	}
	
	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		Species species = getSpecies(world, pos, state);
		if(canBlockStay(world, pos, state)) {
			//Ensure planting conditions are right
			TreeFamily tree = species.getFamily();
			if(world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState());//set to a single branch
				species.placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty sand underneath
			}
		} else {
			dropBlock(world, species, state, pos);
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.5f, 0.625f);
	}

}
