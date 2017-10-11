package com.ferreusveritas.dynamictrees.api.backport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockState {
	
	///////////////////////////////////////////
	// GETTERS/SETTERS
	///////////////////////////////////////////
	
	Block getBlock();
	int getMeta();
	IBlockState withMeta(int meta);
	void setInWorld(World world, BlockPos pos, int flags); 
	void setInWorld(World world, BlockPos pos);
	IBlockState getFromWorld(IBlockAccess blockAccess, BlockPos pos);

	///////////////////////////////////////////
	// COMPARISONS
	///////////////////////////////////////////
	
	boolean equals(IBlockState otherState);
	boolean equals(Block block, int meta);
	boolean equals(Block block);
	boolean equals(int meta);
	boolean matches(IBlockState otherState, int mask);
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side);

	///////////////////////////////////////////
	// ITEMSTACK CONVERTORS
	///////////////////////////////////////////

	public ItemStack toItemStack(int qty);
	public ItemStack toItemStack();
	
}
