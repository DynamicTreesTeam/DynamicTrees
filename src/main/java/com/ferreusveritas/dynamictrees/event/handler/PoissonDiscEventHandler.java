package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
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
        final LevelAccessor level = event.getWorld();
        if (!level.isClientSide()) {
            DynamicTreeFeature.DISC_PROVIDER.unloadWorld((ServerLevel) level);//clears the circles
        }
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        final LevelAccessor level = event.getWorld();

		if (level == null || level.isClientSide()) {
			return;
		}

        final byte[] circleData = event.getData().getByteArray(CIRCLE_DATA_ID);
        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;

        final ChunkPos chunkPos = event.getChunk().getPos();
        discProvider.setChunkPoissonData(LevelContext.create(level), chunkPos, circleData);
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        final LevelContext levelContext = LevelContext.create(event.getWorld());
        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
        final ChunkAccess chunk = event.getChunk();
        final ChunkPos chunkPos = chunk.getPos();

        final byte[] circleData = discProvider.getChunkPoissonData(levelContext, chunkPos);
        event.getData().putByteArray(CIRCLE_DATA_ID, circleData); // Set circle data.

		if (chunk instanceof LevelChunk && !((LevelChunk) chunk).loaded) {
			discProvider.unloadChunkPoissonData(levelContext, chunkPos);
		}
    }

}
