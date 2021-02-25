package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.client.TooltipHandler;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.event.FutureBreak;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.worldgen.canceller.TreeCancellerJson;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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

		if(event.type == TickEvent.Type.WORLD && event.phase == TickEvent.Phase.START) {
			SeasonHelper.updateTick(event.world, event.world.getDayTime());
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()){
			DTClient.discoverWoodColors();
		}
	}

	@SubscribeEvent
	public void onWorldUnload (WorldEvent.Unload event) {
		TreeCancellerJson.INSTANCE = null; // Reset tree canceller Json.
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onItemTooltipAdded(ItemTooltipEvent event) {
		TooltipHandler.setupTooltips(event);
	}

}