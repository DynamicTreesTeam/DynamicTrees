package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;

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
		
		if(WorldGenRegistry.isWorldGenEnabled() && !event.getWorld().isRemote()) {
//			if(!WorldGenRegistry.validateBiomeDataBases()) {
				WorldGenRegistry.populateDataBase();
//			}
		}
		
		//		event.getWorld().addEventListener(new WorldListener(event.getWorld(), event.getWorld().ser()));
	}
	
}