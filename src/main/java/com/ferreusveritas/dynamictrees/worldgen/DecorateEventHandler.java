package com.ferreusveritas.dynamictrees.worldgen;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenForest;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

/**
* <pre><tt>
* [   01   ] [   02   ] [   03   ] [   04   ]
* ┌──┬──┬──┐ ┌──┬──┬──┐ ┌──┬──┬──┐ ┌──┬──┬──┐
* │  │  │  │ │  │  │  │ │  │Un│Ex│ │Un│Ex│  │
* ├──┼──┼──┤ ├──┼──┼──┤ ├──┼──┼──┤ ├──┼──┼──┤
* │  │Un│Ex│ │Un│  │  │ │  │  │Ex│ │Ex│Un│  │
* ├──┼──┼──┤ ├──┼──┼──┤ ├──┼──┼──┤ ├──┼──┼──┤
* │  │Ex│Ex│ │Ex│Ex│  │ │  │  │  │ │  │  │  │
* └──┴──┴──┘ └──┴──┴──┘ └──┴──┴──┘ └──┴──┴──┘
* </tt></pre>
*
* Ex: Chunk that will be tested for existence
* Un: Undecorated chunk that will be decorated if the other chunks exist.
* 
* The center chunk was just created it is guaranteed to exist.
*
*/
public class DecorateEventHandler {

	public DecorateEventHandler() {
		disableRoofedForest();
	}

	private void disableRoofedForest() {
		//I didn't want to have to do this but you've given me no choice. This is only necessary in 1.7.10
		//BiomeGenForest(the secret of the Roofed Forest): BiomeGenForest.field_150632_aF == 3; see: new BiomeGenForest(29(BiomeID), 3) in BiomeGenBase
		ReflectionHelper.setPrivateValue(BiomeGenForest.class, (BiomeGenForest)BiomeGenBase.roofedForest, 0, 0);//Modify field_150632_aF and revoke roofedForest's privileges.
	}
		
	//Seeds:
	//Forest, Roofed Forest, Desert, Village: 3036345388435907851
	//Swamp, Roofed Forest, Desert, Forest, Birch Forest, Village 1212426086214323691
	//Roofed Forest Seed:  1488218954

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(DecorateBiomeEvent.Decorate event) {
		if(event.type == EventType.TREE) {//Does not affect roofed forests in 1.7.10
			BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX * 16, event.chunkZ * 16);
			String biomeCanonicalName = biome.getClass().getCanonicalName();
			//Only deny tree decoration for Vanilla Minecraft Biomes
			if(biomeCanonicalName != null && biomeCanonicalName.startsWith("net.minecraft.world.biome.")) {
				event.setResult(Result.DENY);
			}
		}
	}
}