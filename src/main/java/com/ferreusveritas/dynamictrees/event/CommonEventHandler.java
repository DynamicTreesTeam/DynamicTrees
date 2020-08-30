package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonEventHandler {

	SeasonManager seasonManager = new SeasonManager();

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
		if(WorldGenRegistry.isWorldGenEnabled() && !event.getWorld().isRemote()) {
			if(!WorldGenRegistry.validateBiomeDataBases()) {
				WorldGenRegistry.populateDataBase();
			}
		}

//		event.getWorld().addEventListener(new WorldListener(event.getWorld(), event.getWorld().ser()));
	}

}