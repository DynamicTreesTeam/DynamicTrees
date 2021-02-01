package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.logging.Level;
import java.util.logging.Logger;

@Mod.EventBusSubscriber
public class MissingEventHandler {

	///////////////////////////////////////////
	// MISSING REMAPPING
	///////////////////////////////////////////

	/**
	 * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
	 * 
	 * @param event
	 */
	//Missing Blocks Resolved Here
	@SubscribeEvent
	public void missingBlockMappings(MissingMappings<Block> event) {
		for(Mapping<Block> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getNamespace();
			String path = resLoc.getPath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(DynamicTrees.MODID).log(Level.CONFIG, "Remapping Missing Block: " + path);
				Block mappedBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(DynamicTrees.MODID, path));
				if(mappedBlock != Blocks.AIR) { //Air is what you get when do don't get what you're looking for.
					assert mappedBlock != null;
					missing.remap(mappedBlock);
				}
			}
		}
	}

	//Missing Items Resolved Here
	@SubscribeEvent
	public void missingItemMappings(MissingMappings<Item> event) {
		for(Mapping<Item> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getNamespace();
			String path = resLoc.getPath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(DynamicTrees.MODID).log(Level.CONFIG, "Remapping Missing Item: " + path);
				Item mappedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(DynamicTrees.MODID, path));
				if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
					missing.remap(mappedItem);
				}
			}
		}
	}
	
}
