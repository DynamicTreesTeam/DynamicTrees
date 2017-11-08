package com.ferreusveritas.dynamictrees.compat;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxyCompat {

	public CCProxyBase ccproxy;
	
	public void preInit() {
		//Computercraft Creative Mode Stuff
		ccproxy = CCProxyBase.hasComputerCraft() ? new CCProxyActive() : new CCProxyBase();
		ccproxy.createBlocks();
	}

	public void init() {}
	
	public void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		
		ccproxy.registerBlocks(registry);
	}
	
	public void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();

		ccproxy.registerItems(registry);		
	}

	public void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
		final IForgeRegistry<IRecipe> registry = event.getRegistry();
		
		ccproxy.registerRecipes(registry);
	}
	
}
