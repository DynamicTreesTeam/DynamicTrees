package com.ferreusveritas.growingtrees.event;

import com.ferreusveritas.growingtrees.worldgen.ChunkCircleManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;

public class CommonEventHandler {

	/** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

	/** We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:( */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.world.provider.dimensionId == 0 && !event.world.isRemote) {
			ChunkCircleManager.newInstance();
		}
	}

	@SubscribeEvent 
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		if(event.world.provider.dimensionId == 0){//Overworld
			ChunkCircleManager cm = ChunkCircleManager.getInstance();
			byte circleData[] = event.getData().getByteArray("GTCD");   	
			cm.setChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition, circleData);
		}
	}

	/** Do not use this to unload circles..  This is called before the onChuckDataSave event. */
	/*@SubscribeEvent 
	public void onChunkUnload(ChunkEvent.Unload event) {}*/ 

	@SubscribeEvent 
	public void onChunkDataSave(ChunkDataEvent.Save event) {

		if(event.world.provider.dimensionId == 0) {//Overworld
			ChunkCircleManager cm = ChunkCircleManager.getInstance();
			byte circleData[] = cm.getChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition);
			NBTTagByteArray circleByteArray = new NBTTagByteArray(circleData);
			event.getData().setTag("GTCD", circleByteArray);//Growing Trees Circle Data

			// Unload circles here if the chunk is no longer loaded.
			if(!event.getChunk().isChunkLoaded) {
				cm.unloadChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition);
			}
		}
		
	}

}
