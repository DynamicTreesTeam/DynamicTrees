package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class CommonEventHandler {

	SeasonManager seasonManager = new SeasonManager();

//	@SubscribeEvent
//	public void onLoadComplete(FMLLoadCompleteEvent event){
//		DTClient.discoverWoodColors();
//	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {

		if(event.side == LogicalSide.SERVER) {
			FutureBreak.process(event.world);
		}

		if(event.type == TickEvent.Type.WORLD && event.phase == TickEvent.Phase.START && event.world.getDimension().isSurfaceWorld()) {
			seasonManager.updateTick(event.world, event.world.getGameTime());
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()){
			DTClient.discoverWoodColors();
		}

		if(WorldGenRegistry.isWorldGenEnabled() && !event.getWorld().isRemote()) {
			if(!WorldGenRegistry.validateBiomeDataBases()) {
				WorldGenRegistry.populateDataBase();
			}
		}

//		event.getWorld().addEventListener(new WorldListener(event.getWorld(), event.getWorld().ser()));
	}

}