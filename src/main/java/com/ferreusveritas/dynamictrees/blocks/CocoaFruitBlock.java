package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CocoaFruitBlock extends CocoaBlock {

	public CocoaFruitBlock() {
		super(Block.Properties.of(Material.PLANT)
				.randomTicks()
				.strength(0.2F, 3.0F)
				.sound(SoundType.WOOD));
	}
	
	/**
	* Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
	*/
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState logState = worldIn.getBlockState(pos.relative(state.getValue(FACING)));
		BranchBlock branch = TreeHelper.getBranch(logState);
		return branch != null && branch.getRadius(logState) == 8 && branch.getFamily().canSupportCocoa;
	}

}
