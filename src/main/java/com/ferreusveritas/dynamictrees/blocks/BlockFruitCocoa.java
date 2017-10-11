package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFruitCocoa extends BlockCocoa {
	
	public BlockFruitCocoa() {
		this("fruitcocoa");
	}
	
	public BlockFruitCocoa(String name) {
		setRegistryName(name);
	}
	
	/**
	* Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
	*/
	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {		
		pos = pos.offset(state.getValue(FACING));
		BlockBranch branch = TreeHelper.getBranch(world, pos);
		return branch != null && branch.getRadius(world, pos) == 8 && branch.getTree().canSupportCocoa;
	}

}
