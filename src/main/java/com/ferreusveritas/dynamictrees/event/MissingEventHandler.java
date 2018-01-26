package com.ferreusveritas.dynamictrees.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod.EventHandler;

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
	@EventHandler
	public void missingBlockMappings(MissingMappings<Block> event) {
		for(Mapping<Block> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(ModConstants.MODID).log(Level.CONFIG, "Remapping Missing Block: " + path);
				Block mappedBlock = Block.REGISTRY.getObject(new ResourceLocation(ModConstants.MODID, path));
				if(mappedBlock != Blocks.AIR) { //Air is what you get when do don't get what you're looking for.
					missing.remap(mappedBlock);
				}
			}
		}
	}

	//Missing Items Resolved Here
	@EventHandler
	public void missingItemMappings(MissingMappings<Item> event) {		
		for(Mapping<Item> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(ModConstants.MODID).log(Level.CONFIG, "Remapping Missing Item: " + path);
				Item mappedItem = Item.REGISTRY.getObject(new ResourceLocation(ModConstants.MODID, path));
				if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
					missing.remap(mappedItem);
				}
			}
		}
	}
	
}
