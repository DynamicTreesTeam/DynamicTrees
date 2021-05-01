package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class DynamicCocoaBlock extends CocoaBlock {

	public DynamicCocoaBlock() {
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

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(Items.COCOA_BEANS);
	}
}
