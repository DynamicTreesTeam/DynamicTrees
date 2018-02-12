package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import net.minecraft.block.BlockSand;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState extState = (IExtendedBlockState) state;
			
			final int dMap[] = {0, -1, 1};
			
			IBlockState mimic = Blocks.SAND.getDefaultState(); // Default to sand
			
			for (int depth : dMap) {
				for (EnumFacing dir : EnumFacing.HORIZONTALS) {
					IBlockState ground = world.getBlockState(pos.offset(dir).down(depth));
					
					if (ground.getBlock() == Blocks.SAND && ground.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND) {
						return extState.withProperty(MIMIC, ground); // Prioritize red sand
					}
					if (ground.getBlock() instanceof BlockSand && ground.getBlock() != Blocks.SAND) {
						return extState.withProperty(MIMIC, ground); // Prioritize other modded sand
					}
				}
			}
			return extState.withProperty(MIMIC, mimic);
		}
		return state;
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	public IBlockState getDecayBlockState(IBlockAccess access, BlockPos pos) {
		return Blocks.SAND.getDefaultState();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.SAND);
	}
	
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
}
