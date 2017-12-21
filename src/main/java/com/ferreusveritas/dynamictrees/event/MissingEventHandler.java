package com.ferreusveritas.dynamictrees.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;		

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
	public void missingBlockMappings(FMLMissingMappingsEvent event) {
		for(MissingMapping missing: event.getAll()) {
			if(missing.type == GameRegistry.Type.BLOCK) {
				ResourceLocation resLoc = new ResourceLocation(missing.name);
				String domain = resLoc.getResourceDomain();
				String path = resLoc.getResourcePath();
				if(domain.equals("growingtrees")) {
					Logger.getLogger(ModConstants.MODID).log(Level.CONFIG, "Remapping Missing Object: " + path);
					Block mappedBlock = cpw.mods.fml.common.registry.GameRegistry.findBlock(ModConstants.MODID, path);
					if(mappedBlock != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedBlock);
					}
				}
			}
		}
	}

	//Missing Items Resolved Here
	@EventHandler
	public void missingItemMappings(FMLMissingMappingsEvent event) {		
		for(MissingMapping missing: event.getAll()) {
			if(missing.type == GameRegistry.Type.ITEM) {
				ResourceLocation resLoc = new ResourceLocation(missing.name);
				String domain = resLoc.getResourceDomain();
				String path = resLoc.getResourcePath();
				if(domain.equals("growingtrees")) {
					Logger.getLogger(ModConstants.MODID).log(Level.CONFIG, "Remapping Missing Object: " + path);
					Item mappedItem = cpw.mods.fml.common.registry.GameRegistry.findItem(ModConstants.MODID, path);
					if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedItem);
					}
				}
			}
		}
	}
	
}
