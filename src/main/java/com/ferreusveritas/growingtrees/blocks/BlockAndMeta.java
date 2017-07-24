package com.ferreusveritas.growingtrees.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockAndMeta {

	private Block block;
	private int meta;

	public BlockAndMeta(){
		set(Blocks.dirt, 0);
	}

	public BlockAndMeta(Block block, int meta) {
		set(block).set(meta);
	}

	public BlockAndMeta(IBlockAccess blockAccess, int x, int y, int z){
		setFromCoords(blockAccess, x, y, z);
	}

	public Block getBlock(){
		return block;
	}

	public int getMeta(){
		return meta;
	}

	public BlockAndMeta set(Block block, int meta){
		return set(block).set(meta);
	}

	public BlockAndMeta set(Block block){
		this.block = block;
		return this;
	}

	public BlockAndMeta set(int meta){
		this.meta = meta;
		return this;
	}

	public BlockAndMeta setFromCoords(IBlockAccess blockAccess, int x, int y, int z){
		return set(blockAccess.getBlock(x, y, z), blockAccess.getBlockMetadata(x, y, z));
	}

	//Comparison functions for other vanilla blocks
	public boolean equals(BlockAndMeta other){
		return getBlock() == other.getBlock() && getMeta() == other.getMeta();
	}

	public boolean matches(IBlockAccess blockAccess, int x, int y, int z, int mask){
		return matches(blockAccess.getBlock(x, y, z), blockAccess.getBlockMetadata(x, y, z) & mask);
	}

	public boolean matches(IBlockAccess blockAccess, int x, int y, int z){
		return matches(blockAccess,x, y, z, 7);
	}

	public boolean matches(Block block, int meta){
		return matches(block) && matches(meta);
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
