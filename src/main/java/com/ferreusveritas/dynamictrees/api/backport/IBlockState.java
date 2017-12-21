package com.ferreusveritas.dynamictrees.api.backport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public interface IBlockState {
	
	public Block getBlock();
	public int getMeta();
	public IBlockState withMeta(int meta);
	public IBlockState withProperty(IProperty property, int value);
	public int getValue(IProperty property);
	
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
	
	public Material getMaterial();
	public boolean isFullCube();
	public boolean isOpaqueCube();
}
