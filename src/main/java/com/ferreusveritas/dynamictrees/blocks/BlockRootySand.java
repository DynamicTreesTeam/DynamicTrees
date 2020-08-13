package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.Random;

public class BlockRootySand extends BlockRooty {
	
	static String name = "rootysand";
	
	public BlockRootySand(boolean isTileEntity) {
		this(name + (isTileEntity ? "species" : ""), isTileEntity);
	}
	
	public BlockRootySand(String name, boolean isTileEntity) {
		super(name, Material.SAND, isTileEntity);
//		setSoundType(SoundType.SAND);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	public BlockState getMimic(IBlockReader access, BlockPos pos) {
		return MimicProperty.getSandMimic(access, pos);
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	public BlockState getDecayBlockState(IBlockReader access, BlockPos pos) {
		return Blocks.SAND.getDefaultState();
	}
	
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return Item.getItemFromBlock(Blocks.SAND);
//	}
//
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.SOLID;
	}
	
}
