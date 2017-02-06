package com.ferreusveritas.growingtrees.worldgen;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
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
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    
    public void onEvent(DecorateBiomeEvent.Decorate event) {
    	//System.out.println("Decorate Event:" + event.chunkZ + "," + event.chunkZ + " - " + event.type.toString());
    	
    	if(event.type == EventType.TREE){//Does not affect roofed forests
    		event.setResult(Result.DENY);
    	}

    	/*if(event.type == EventType.BIG_SHROOM){//For mushroom islands.. has no affect on RoofedForests
    		event.setResult(Result.DENY);
    	}*/
    	    	
    	//Roofed Forest Seed:  1488218954
    	//BiomeGenForest(the secret of the Roofed Forest): this.field_150632_aF == 3,   new BiomeGenForest(29(BiomeID), 3)
    	
    }
}