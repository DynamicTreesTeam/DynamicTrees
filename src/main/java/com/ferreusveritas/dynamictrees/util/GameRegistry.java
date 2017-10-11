package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.items.ItemBlockLore;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class GameRegistry extends cpw.mods.fml.common.registry.GameRegistry {

	public static void register(IRegisterable object) {
		if(object instanceof Block) {
			registerBlock((Block) object, object instanceof ILorable ? ItemBlockLore.class : ItemBlock.class, object.getRegistryName());
		} else
		if(object instanceof Item) {
			registerItem((Item) object, object.getRegistryName());
		}
	}

	/** Not a real brewing recipe.  But we do what we can for 1.7.10 */
	public static void addBrewingRecipe(ItemStack potion, ItemStack reactant, ItemStack output) {
		GameRegistry.addShapelessRecipe(output,	potion, reactant);
	}
	
}
