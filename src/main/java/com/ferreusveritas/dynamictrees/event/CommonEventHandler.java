package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.seasons.SeasonManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class CommonEventHandler {
	
	SeasonManager seasonManager = new SeasonManager();
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.type == Type.WORLD && event.phase == Phase.START && event.world.provider.dimensionId == 0) {
			seasonManager.updateTick(event.world, event.world.getWorldTime());
		}
	}
	
}