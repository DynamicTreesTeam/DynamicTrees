package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public class CCProxyActive extends CCProxyBase {

	BlockDendroCoil blockDendroCoil;
	
	public CCProxyActive() {}
	
	@Override
	public void createBlocks() {
		blockDendroCoil = new BlockDendroCoil();
	}
	
	@Override
	public void createItems() {}
	
	@Override
	public void registerBlocks(IForgeRegistry<Block> registry) {
		registry.register(blockDendroCoil);
	}
	
	@Override
	public void registerItems(IForgeRegistry<Item> registry) {
		ItemBlock itemBlock = new ItemBlock(blockDendroCoil);
		itemBlock.setRegistryName(blockDendroCoil.getRegistryName());
		registry.register(itemBlock);
	}
	
}
