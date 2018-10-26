package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.worldgen.PoissonDiscProviderUniversal;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PoissonDiscEventHandler {

	/** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

	/** We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:( */
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		World world = event.getWorld();
		if(!world.isRemote) {
			TreeGenerator.getTreeGenerator().getCircleProvider().unloadWorld(world);//clears the circles
		}
	}

	@SubscribeEvent 
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		World world = event.getWorld();
		if(!world.isRemote) {
			byte circleData[] = event.getData().getByteArray("GTCD");
			PoissonDiscProviderUniversal cp = TreeGenerator.getTreeGenerator().getCircleProvider();
			cp.setChunkPoissonData(world, event.getChunk().x, 0, event.getChunk().z, circleData);
		}
	}
	
	@SubscribeEvent 
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		World world = event.getWorld();
		PoissonDiscProviderUniversal cp = TreeGenerator.getTreeGenerator().getCircleProvider();
		byte circleData[] = cp.getChunkPoissonData(world, event.getChunk().x, 0, event.getChunk().z);
		NBTTagByteArray circleByteArray = new NBTTagByteArray(circleData);
		event.getData().setTag("GTCD", circleByteArray);//Growing Trees Circle Data
		
		// Unload circles here if the chunk is no longer loaded.
		if(!event.getChunk().isLoaded()) {
			cp.unloadChunkPoissonData(world, event.getChunk().x, 0, event.getChunk().z);
		}
		
	}

}
