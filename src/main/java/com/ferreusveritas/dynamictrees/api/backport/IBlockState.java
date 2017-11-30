package com.ferreusveritas.dynamictrees.api.backport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public interface IBlockState {
	
	public Block getBlock();
	public int getMeta();
	public IBlockState withMeta(int meta);
	
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
