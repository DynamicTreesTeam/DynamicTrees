package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler {

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		DynamicTrees.proxy.registerModels();
	}

}