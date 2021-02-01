package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;

import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeCancellerJson;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonEventHandler {
	
	//	@SubscribeEvent
	//	public void onLoadComplete(FMLLoadCompleteEvent event){
	//		DTClient.discoverWoodColors();
	//	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.side == LogicalSide.SERVER) {
			FutureBreak.process(event.world);
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()){
			DTClient.discoverWoodColors();
		}
		
		if (!event.getWorld().isRemote() && WorldGenRegistry.isWorldGenEnabled()) {
//			if (!WorldGenRegistry.validateBiomeDataBases()) {
			WorldGenRegistry.populateDataBase();
//			}
		}
		
		//		event.getWorld().addEventListener(new WorldListener(event.getWorld(), event.getWorld().ser()));
	}

	@SubscribeEvent
	public void onWorldUnload (WorldEvent.Unload event) {
		TreeCancellerJson.INSTANCE = null; // Reset tree canceller Json.
	}
	
}