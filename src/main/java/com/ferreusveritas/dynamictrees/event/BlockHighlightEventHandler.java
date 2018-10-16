package com.ferreusveritas.dynamictrees.event;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHighlightEventHandler {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onBlockHighlightEvent(DrawBlockHighlightEvent event) {
		
	}

}
