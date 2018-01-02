package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDynamicSaplingSpecies extends BlockDynamicSapling implements ITileEntityProvider {

	public BlockDynamicSaplingSpecies(String name) {
		super(name);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}

	
	
}
