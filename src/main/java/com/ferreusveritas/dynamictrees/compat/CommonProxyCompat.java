package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxyCompat {
	
	public void preInit() {
		BlockDynamicLeaves.passableLeavesModLoaded = net.minecraftforge.fml.common.Loader.isModLoaded("passableleaves");
	}
	
	public void init() {}
	
	public void registerBlocks(IForgeRegistry<Block> registry) {
	}
	
	public void registerItems(IForgeRegistry<Item> registry) {
	}
	
	public void registerRecipes(IForgeRegistry<IRecipe> registry) {
	}
	
}
