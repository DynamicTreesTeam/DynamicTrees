package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DecorateEventHandler {
	
	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(DecorateBiomeEvent.Decorate event) {
		if(!ModConfigs.dimensionBlacklist.contains(event.getWorld().provider.getDimension())) {
			Biome biome = event.getWorld().getBiome(event.getPos());
			switch(event.getType()) {
				case CACTUS:
					if(!ModConfigs.vanillaCactusWorldGen && TreeGenerator.getTreeGenerator().biomeDataBase.getEntry(biome).shouldCancelVanillaTreeGen()) {
						event.setResult(Result.DENY);
					} 
					break;
				case BIG_SHROOM:
				case TREE://Cactus is also done by the tree generator
					if(TreeGenerator.getTreeGenerator().biomeDataBase.getEntry(biome).shouldCancelVanillaTreeGen()) {
						event.setResult(Result.DENY);
					}
				default: break;
			}
		}
	}
}