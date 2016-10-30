package com.ferreusveritas.growingtrees.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockAndMeta {

	private Block block;
	private int meta;

	public BlockAndMeta(Block block, int meta) {
		this.block = block;
		this.meta = meta;
	}
	
	public Block getBlock(){
		return block;
	}
	
	public int getMeta(){
		return meta;
	}
	
	//Comparison functions for other vanilla blocks
	public boolean equals(BlockAndMeta other){
		return getBlock() == other.getBlock() && getMeta() == other.getMeta();
	}
	
	public boolean matches(IBlockAccess blockAccess, int x, int y, int z, int mask){
		return matches(blockAccess.getBlock(x, y, z), blockAccess.getBlockMetadata(x, y, z) & mask);
	}

	public boolean matches(IBlockAccess blockAccess, int x, int y, int z){
		return matches(blockAccess,x, y, z, 3);
	}
	
	public boolean matches(Block block, int meta){
		return block == getBlock() && meta == getMeta();
	}

	public boolean matches(Block block){
		return block == getBlock();
	}

	public boolean matches(int meta){
		return meta == getMeta();
	}

	
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side) {
		return block.getIcon(side, meta);
	}
	
	public ItemStack toItemStack(int qty){
		return new ItemStack(block, qty, meta);
	}
	
	public ItemStack toItemStack(){
        return toItemStack(1);
	}
}
