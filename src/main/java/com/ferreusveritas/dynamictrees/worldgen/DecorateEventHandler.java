package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DecorateEventHandler {

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(DecorateBiomeEvent.Decorate event) {
		if(!ModConfigs.dimensionBlacklist.contains(event.getWorld().provider.getDimension())) {
			switch(event.getType()) {
				case CACTUS: if(ModConfigs.vanillaCactusWorldGen) { break; } 
				case TREE:
					Biome biome = event.getWorld().getBiome(event.getPos());
					if(TreeGenerator.getTreeGenerator().biomeDataBase.getEntry(biome).shouldCancelVanillaTreeGen()) {
						event.setResult(Result.DENY);
					}
					TreeGenerator.getTreeGenerator().generate(event.getWorld(), biome, new ChunkPos(event.getPos()));
					break;
				case BIG_SHROOM:
					//We need to disable Giant Mushroom creation until after the trees are built
					if(BiomeDictionary.hasType(event.getWorld().getBiome(event.getPos()), Type.SPOOKY)) {
						event.setResult(Result.DENY);//Disable shrooms for roofedForest only
					}
				default: break;
			}
		}
	}
}