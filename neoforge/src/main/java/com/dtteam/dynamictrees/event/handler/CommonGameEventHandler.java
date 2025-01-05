package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public class CommonGameEventHandler {

    ///////////////////////////////////////////
    // LEVEL
    ///////////////////////////////////////////

    public static final String CIRCLE_DATA_ID = "GTCD"; // ID for "Growing Trees Circle Data" NBT tag.

    @SubscribeEvent
    public static void onPreLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide) {
            FutureBreak.process(event.getLevel());
        }
        SeasonHelper.updateTick(event.getLevel(), event.getLevel().getDayTime());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ClientModEventHandler.discoverWoodColors();
        }
        //        BiomeDatabases.populateBlacklistFromConfig();
    }

    /**
     * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        final LevelAccessor level = event.getLevel();
//        if (!level.isClientSide()) {
//            DynamicTreeFeature.DISC_PROVIDER.unloadWorld((ServerLevel) level);//clears the circles
//        }
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
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
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
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

    ///////////////////////////////////////////
    // SERVER
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onServerStart(final ServerStartingEvent event) {
        SeasonHelper.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

    ///////////////////////////////////////////
    // RESOURCES
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new Resources.ReloadListener(event.getServerResources()));
    }

}