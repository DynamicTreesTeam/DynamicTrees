package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.worldgen.ChunkCircleManager;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CircleEventHandler {

	/** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

	/** We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:( */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		World world = event.getWorld();
		if(world.provider.getDimension() == 0 && !world.isRemote) {
			TreeGenerator.getTreeGenerator().onWorldUnload();//clears the circles
		}
	}

	@SubscribeEvent 
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		if(event.getWorld().provider.getDimension() == 0){//Overworld
			byte circleData[] = event.getData().getByteArray("GTCD");
			TreeGenerator.getTreeGenerator().getChunkCircleManager().setChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition, circleData);
		}
	}

	/** Do not use this to unload circles..  This is called before the onChuckDataSave event. */
	/*@SubscribeEvent 
	public void onChunkUnload(ChunkEvent.Unload event) {}*/ 

	@SubscribeEvent 
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		if(event.getWorld().provider.getDimension() == 0) {//Overworld
			ChunkCircleManager cm = TreeGenerator.getTreeGenerator().getChunkCircleManager();
			byte circleData[] = cm.getChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition);
			NBTTagByteArray circleByteArray = new NBTTagByteArray(circleData);
			event.getData().setTag("GTCD", circleByteArray);//Growing Trees Circle Data

			// Unload circles here if the chunk is no longer loaded.
			if(!event.getChunk().isLoaded()) {
				cm.unloadChunkCircleData(event.getChunk().xPosition, event.getChunk().zPosition);
			}
		}
	}

}
