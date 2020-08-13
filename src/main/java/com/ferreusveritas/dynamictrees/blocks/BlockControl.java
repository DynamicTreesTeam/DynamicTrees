package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;

public class BlockControl extends Block {
	
	public BlockControl() {
		super(Properties.create(Material.AIR));
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return false;
	}

}
