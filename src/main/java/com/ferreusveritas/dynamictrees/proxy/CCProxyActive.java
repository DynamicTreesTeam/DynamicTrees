package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
	public void registerBlocks() {
		GameRegistry.register(blockDendroCoil);
	}
	
	@Override
	public void registerItems() {
		ItemBlock itemBlock = new ItemBlock(blockDendroCoil);
		itemBlock.setRegistryName(blockDendroCoil.getRegistryName());
		GameRegistry.register(itemBlock);
	}
	
}
