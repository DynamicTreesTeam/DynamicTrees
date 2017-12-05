package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModRecipes {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
		final IForgeRegistry<IRecipe> registry = event.getRegistry();

		ModItems.dendroPotion.registerRecipes(registry);
		ModItems.dirtBucket.registerRecipes(registry);
		
		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.registerRecipes(registry);
		}
		
		DynamicTrees.compatProxy.registerRecipes(event);
	}
	
}
