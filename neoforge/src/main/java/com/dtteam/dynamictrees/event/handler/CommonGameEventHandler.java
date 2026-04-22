package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.recipe.DendroPotionRecipeHandler;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public class CommonGameEventHandler {

    ///////////////////////////////////////////
    // LEVEL
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onPreLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) {
            FutureBreak.process(event.getLevel());
        }
//        SeasonHelper.updateTick(event.getLevel(), event.getLevel().getDayTime());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ClientModEventHandler.discoverWoodColors();
        } else {
            BiomeDatabases.populateBlacklistFromConfig();
        }
    }

    /**
     * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        final LevelAccessor level = event.getLevel();
        if (!level.isClientSide()) {
            DynamicTreeFeature.DISC_PROVIDER.unloadWorld((ServerLevel) level);//clears the circles
        }
    }

    //TODO: use Data Attachments
//    @SubscribeEvent
//    @SuppressWarnings("ConstantConditions")
//    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
//        if (!DTConfigs.SERVER.worldGen.get()) return;
//
//        final LevelAccessor level = event.getLevel();
//
//		if (level == null || level.isClientSide()) return;
//
//        final byte[] circleData = readChunkByteArray(event.getData(), UniversalPoissonDiscProvider.CIRCLE_DATA_ID);
//        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
//
//        final ChunkPos chunkPos = event.getChunk().getPos();
//        discProvider.setChunkPoissonData(LevelContext.create(level), chunkPos, circleData);
//    }
//
//    @SubscribeEvent
//    public static void onChunkDataSave(ChunkDataEvent.Save event) {
//        if (!DTConfigs.SERVER.worldGen.get()) return;
//
//        final LevelContext levelContext = LevelContext.create(event.getLevel());
//        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
//        final ChunkAccess chunk = event.getChunk();
//        final ChunkPos chunkPos = chunk.getPos();
//
//        final byte[] circleData = discProvider.getChunkPoissonData(levelContext, chunkPos);
//        writeChunkByteArray(event.getData(), UniversalPoissonDiscProvider.CIRCLE_DATA_ID, circleData); // Set circle data.
//
//		if (chunk instanceof LevelChunk && !((LevelChunk) chunk).loaded) {
//			discProvider.unloadChunkPoissonData(levelContext, chunkPos);
//		}
//    }
//
//    private static byte[] readChunkByteArray(SerializableChunkData data, String key){
//        CompoundTag tag = data.attachmentData();
//        try {
//            assert tag != null;
//            if (tag.contains(key)) return ((ByteArrayTag) Objects.requireNonNull(tag.get(key))).getAsByteArray();
//        } catch (ClassCastException classcastexception) {
//            throw new NbtException(String.format("Exception loading %s tag from Chunk Data", key));
//        }
//
//        return new byte[0];
//    }
//
//    private static void writeChunkByteArray(SerializableChunkData data, String key, byte[] bytes){
//        CompoundTag tag = data.attachmentData();
//        try {
//            assert tag != null;
//            tag.put(key, new ByteArrayTag(bytes));
//        } catch (AssertionError exception) {
//            throw new NbtException(String.format("Exception saving %s tag into Chunk Data", key));
//        }
//    }

    ///////////////////////////////////////////
    // SERVER
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onServerStart(final ServerStartingEvent event) {
        SeasonCompatibilityHandler.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

    ///////////////////////////////////////////
    // RESOURCES
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void addReloadListeners(final AddServerReloadListenersEvent event) {
        event.addListener(DynamicTrees.location("resource_reload_listener"), new Resources.ReloadListener(event.getServerResources().getRecipeManager()));
    }

    ///////////////////////////////////////////
    // REGISTRY
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        DendroPotionRecipeHandler.getAllDendroRecipes().forEach(
                recipe -> event.getBuilder().addRecipe(recipe));
    }

}