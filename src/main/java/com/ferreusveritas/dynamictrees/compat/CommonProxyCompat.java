package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxyCompat {

	public CCProxyBase ccproxy;
	
	public void preInit() {
		BlockDynamicLeaves.passableLeavesModLoaded = net.minecraftforge.fml.common.Loader.isModLoaded("passableleaves");
		
		//Computercraft Creative Mode Stuff
		ccproxy = CCProxyBase.hasComputerCraft() ? new CCProxyActive() : new CCProxyBase();
		ccproxy.createBlocks();
	}

	public void init() {}
	
	public void registerBlocks(IForgeRegistry<Block> registry) {
		ccproxy.registerBlocks(registry);
	}
	
	public void registerItems(IForgeRegistry<Item> registry) {
		ccproxy.registerItems(registry);		
	}

	public void registerRecipes(IForgeRegistry<IRecipe> registry) {
		ccproxy.registerRecipes(registry);
	}
	
}
