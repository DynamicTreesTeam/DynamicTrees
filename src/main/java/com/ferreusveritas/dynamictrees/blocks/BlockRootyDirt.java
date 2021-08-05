package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockRootyDirt extends BlockRooty {

	static String name = "rootydirt";

	public BlockRootyDirt(boolean isTileEntity) {
		this(name + (isTileEntity ? "species" : ""), isTileEntity);
	}

	public BlockRootyDirt(String name, boolean isTileEntity) {
		super(name, Material.GROUND, isTileEntity);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	@Override
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		return MimicProperty.getDirtMimic(access, pos);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
	}

}
