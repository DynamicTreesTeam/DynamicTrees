package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PoissonDiscEventHandler {

    // TODO: Check ServerWorld casts work in all dimensions and with modded dimensions.

    public static final String CIRCLE_DATA_ID = "GTCD"; // ID for "Growing Trees Circle Data" NBT tag.

    /** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

    /**
     * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
     */
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        final LevelAccessor world = event.getWorld();
        if (!world.isClientSide()) {
            TreeGenerator.getTreeGenerator().getCircleProvider().unloadWorld((ServerLevel) world);//clears the circles
        }
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        final LevelAccessor world = event.getWorld();

		if (world == null || world.isClientSide()) {
			return;
		}

        final byte[] circleData = event.getData().getByteArray(CIRCLE_DATA_ID);
        final UniversalPoissonDiscProvider discProvider = TreeGenerator.getTreeGenerator().getCircleProvider();

        final ChunkPos chunkPos = event.getChunk().getPos();
        discProvider.setChunkPoissonData((ServerLevel) world, chunkPos, circleData);
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        final ServerLevel world = (ServerLevel) event.getWorld();
        final UniversalPoissonDiscProvider discProvider = TreeGenerator.getTreeGenerator().getCircleProvider();
        final ChunkAccess chunk = event.getChunk();
        final ChunkPos chunkPos = chunk.getPos();

        final byte[] circleData = discProvider.getChunkPoissonData(world, chunkPos);
        event.getData().putByteArray(CIRCLE_DATA_ID, circleData); // Set circle data.

		if (chunk instanceof LevelChunk && !((LevelChunk) chunk).loaded) {
			discProvider.unloadChunkPoissonData(world, chunkPos);
		}
    }

}
