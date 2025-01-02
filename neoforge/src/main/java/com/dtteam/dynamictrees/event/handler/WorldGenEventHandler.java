package com.dtteam.dynamictrees.event.handler;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public class WorldGenEventHandler {

    public static final String CIRCLE_DATA_ID = "GTCD"; // ID for "Growing Trees Circle Data" NBT tag.

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event) {
//        BiomeDatabases.populateBlacklistFromConfig();
    }

    /**
     * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
     */
    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        final LevelAccessor level = event.getLevel();
        if (!level.isClientSide()) {
//            DynamicTreeFeature.DISC_PROVIDER.unloadWorld((ServerLevel) level);//clears the circles
        }
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
//        if (!DTConfigs.WORLD_GEN.get()) return;
//
//        final LevelAccessor level = event.getLevel();
//
//		if (level.isClientSide()) {
//			return;
//		}
//
//        final byte[] circleData = event.getData().getByteArray(CIRCLE_DATA_ID);
//        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
//
//        final ChunkPos chunkPos = event.getChunk().getPos();
//        discProvider.setChunkPoissonData(LevelContext.create(level), chunkPos, circleData);
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
//        if (!DTConfigs.WORLD_GEN.get()) return;
//
//        final LevelContext levelContext = LevelContext.create(event.getLevel());
//        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
//        final ChunkAccess chunk = event.getChunk();
//        final ChunkPos chunkPos = chunk.getPos();
//
//        final byte[] circleData = discProvider.getChunkPoissonData(levelContext, chunkPos);
//        event.getData().putByteArray(CIRCLE_DATA_ID, circleData); // Set circle data.
//
//		if (chunk instanceof LevelChunk && !((LevelChunk) chunk).loaded) {
//			discProvider.unloadChunkPoissonData(levelContext, chunkPos);
//		}
    }

}
