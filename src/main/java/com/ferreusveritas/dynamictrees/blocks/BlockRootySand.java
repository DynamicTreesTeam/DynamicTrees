package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockRootySand extends BlockRooty {
	
	static String name = "rootysand";
	
	public BlockRootySand(boolean isTileEntity) {
		this(name + (isTileEntity ? "species" : ""), isTileEntity);
	}
	
	public BlockRootySand(String name, boolean isTileEntity) {
		super(name, Material.SAND, isTileEntity);
		setSoundType(SoundType.SAND);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		return MimicProperty.getSandMimic(access, pos);
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	public IBlockState getDecayBlockState(IBlockAccess access, BlockPos pos) {
		return MimicProperty.getSandMimic(access, pos);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.SAND);
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.SOLID;
	}
	
}
