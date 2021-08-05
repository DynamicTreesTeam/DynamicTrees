package com.ferreusveritas.dynamictrees;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModModels {

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void register(ModelRegistryEvent event) {
		DynamicTrees.proxy.registerModels();
	}

}
