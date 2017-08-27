package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.worldgen.ChunkCircleManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;

public class CommonEventHandler {

	/** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

	/** We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:( */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(DynamicTrees.treeGenerator != null) {
			World world = event.world;
			if(world.provider.dimensionId == 0 && !world.isRemote) {
				DynamicTrees.treeGenerator.onWorldUnload();//clears the circles
			}
		}
	}

	@SubscribeEvent 
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		if(DynamicTrees.treeGenerator != null) {
			if(event.world.provider.dimensionId == 0){//Overworld
				byte circleData[] = event.getData().getByteArray("GTCD");   	
				DynamicTrees.treeGenerator.getChunkCircleManager().setChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition, circleData);
			}
		}
	}

	/** Do not use this to unload circles..  This is called before the onChuckDataSave event. */
	/*@SubscribeEvent 
	public void onChunkUnload(ChunkEvent.Unload event) {}*/ 

	@SubscribeEvent 
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		if(DynamicTrees.treeGenerator != null) {
			if(event.world.provider.dimensionId == 0) {//Overworld
				ChunkCircleManager cm = DynamicTrees.treeGenerator.getChunkCircleManager();
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

}
