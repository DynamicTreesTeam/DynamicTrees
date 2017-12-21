package com.ferreusveritas.dynamictrees.api.backport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class BlockState implements IBlockState {

	private final Block block;
	private final int meta;

	public BlockState(Block block, int meta) {
		this.block = block;
		this.meta = meta & 0x0f;
	}
	
	public BlockState(Block block) {
		this(block, 0);
	}
	
	///////////////////////////////////////////
	// GETTERS/SETTERS
	///////////////////////////////////////////
	
	@Override
	public Block getBlock(){
		return block;
	}

	@Override
	public int getMeta(){
		return meta;
	}

	@Override
	public IBlockState withMeta(int meta) {
		return new BlockState(block, meta & 0x0f);
	}
	
	@Override
	public IBlockState withProperty(IProperty property, int value) {
		return new BlockState(block, property.apply(value, meta));
	}
	
	@Override
	public int getValue(IProperty property) {
		return property.read(meta);
	}
	
	///////////////////////////////////////////
	// COMPARISONS
	///////////////////////////////////////////
	
	@Override
	public boolean equals(IBlockState other){
		return getBlock() == other.getBlock() && getMeta() == other.getMeta();
	}

	@Override
	public boolean equals(Block block, int meta){
		return equals(block) && equals(meta);
	}

	@Override
	public boolean equals(Block block){
		return block == getBlock();
	}

	@Override
	public boolean equals(int meta){
		return meta == getMeta();
	}

	@Override
	public boolean matches(IBlockState otherState, int mask) {
		return (getBlock() == otherState.getBlock()) && ((getMeta() & mask) == (otherState.getMeta() & mask));
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side) {
		return block.getIcon(side, meta);
	}

	///////////////////////////////////////////
	// ITEMSTACK CONVERTORS
	///////////////////////////////////////////
	
	@Override
	public ItemStack toItemStack(int qty){
		return new ItemStack(block, qty, meta);
	}

	@Override
	public ItemStack toItemStack(){
		return toItemStack(1);
	}
	
	@Override
	public Material getMaterial() {
		return block.getMaterial();
	}
	
	@Override
	public boolean isFullCube() {
		return block.renderAsNormalBlock();
	}
	
	@Override
	public boolean isOpaqueCube() {
		return block.isOpaqueCube();
	}
}
