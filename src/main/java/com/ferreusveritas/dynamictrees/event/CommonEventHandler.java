package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CommonEventHandler {
	
	SeasonManager seasonManager = new SeasonManager();
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {

		if(event.side == Side.SERVER) {
			FutureBreak.process(event.world);
		}
		
		if(event.type == Type.WORLD && event.phase == Phase.START && event.world.provider.getDimension() == 0) {
			seasonManager.updateTick(event.world, event.world.getWorldTime());
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(WorldGenRegistry.isWorldGenEnabled() && !event.getWorld().isRemote) {
			if(!WorldGenRegistry.validateBiomeDataBases()) {
				WorldGenRegistry.populateDataBase();
			}
		}
		
		event.getWorld().addEventListener(new WorldListener(event.getWorld(), event.getWorld().getMinecraftServer()));
	}
	
}