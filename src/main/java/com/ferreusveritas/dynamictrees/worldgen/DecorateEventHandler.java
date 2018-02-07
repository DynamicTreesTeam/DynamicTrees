package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
* <pre><tt>
* [   01   ] [   02   ] [   03   ] [   04   ]
* â”Œâ”€â”€â”¬â”€â”€â”¬â”€â”€â”� â”Œâ”€â”€â”¬â”€â”€â”¬â”€â”€â”� â”Œâ”€â”€â”¬â”€â”€â”¬â”€â”€â”� â”Œâ”€â”€â”¬â”€â”€â”¬â”€â”€â”�
* â”‚  â”‚  â”‚  â”‚ â”‚  â”‚  â”‚  â”‚ â”‚  â”‚Unâ”‚Exâ”‚ â”‚Unâ”‚Exâ”‚  â”‚
* â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤
* â”‚  â”‚Unâ”‚Exâ”‚ â”‚Unâ”‚  â”‚  â”‚ â”‚  â”‚  â”‚Exâ”‚ â”‚Exâ”‚Unâ”‚  â”‚
* â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤ â”œâ”€â”€â”¼â”€â”€â”¼â”€â”€â”¤
* â”‚  â”‚Exâ”‚Exâ”‚ â”‚Exâ”‚Exâ”‚  â”‚ â”‚  â”‚  â”‚  â”‚ â”‚  â”‚  â”‚  â”‚
* â””â”€â”€â”´â”€â”€â”´â”€â”€â”˜ â””â”€â”€â”´â”€â”€â”´â”€â”€â”˜ â””â”€â”€â”´â”€â”€â”´â”€â”€â”˜ â””â”€â”€â”´â”€â”€â”´â”€â”€â”˜
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
		//ReflectionHelper.setPrivateValue(BiomeGenForest.class, (BiomeGenForest)BiomeGenBase.roofedForest, 0, 0);//Modify field_150632_aF and revoke roofedForest's privileges.
	}
		
	//Seeds:
	//Forest, Roofed Forest, Desert, Village: 3036345388435907851
	//Swamp, Roofed Forest, Desert, Forest, Birch Forest, Village 1212426086214323691
	//Roofed Forest Seed:  1488218954

	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(DecorateBiomeEvent.Decorate event) {
		if(event.getType() == EventType.TREE || event.getType() == EventType.CACTUS) {//Affects roofed forests in 1.10.2(Not 1.7.10)
			Biome biome = event.getWorld().getBiome(event.getPos());
			ResourceLocation resloc = biome.getRegistryName();
			//Only deny tree decoration for Vanilla Minecraft Biomes
			if(resloc.getResourceDomain().equals("minecraft")) {
				event.setResult(Result.DENY);
			}
		} else
		if(event.getType() == EventType.BIG_SHROOM){
			Biome biome = event.getWorld().getBiome(event.getPos());
			//We need to disable Giant Mushroom creation until after the trees are built
			if(CompatHelper.biomeHasType(biome, Type.SPOOKY)) { //Disable shrooms for roofedForest only
				event.setResult(Result.DENY);
			}
		}
	}
}