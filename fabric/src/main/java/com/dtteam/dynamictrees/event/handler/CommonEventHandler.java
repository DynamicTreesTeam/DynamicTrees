package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.utility.LevelContext;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;

public class CommonEventHandler {

    public static void RegisterEvents(){

        ServerTickEvents.START_WORLD_TICK.register((level)->{
            FutureBreak.process(level);
            SeasonHelper.updateTick(level, level.getDayTime());
        });

        ServerWorldEvents.LOAD.register(((minecraftServer, serverLevel) -> {
            BiomeDatabases.populateBlacklistFromConfig();
        }));
//        if (event.getLevel().isClientSide()) {
//            ClientModEventHandler.discoverWoodColors();
//        }

        ServerWorldEvents.UNLOAD.register(((minecraftServer, serverLevel) -> {
            DynamicTreeFeature.DISC_PROVIDER.unloadWorld(serverLevel);//clears the circles
        }));

        ClientTickEvents.START_WORLD_TICK.register((level)->{
            SeasonHelper.updateTick(level, level.getDayTime());
        });

        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> {
            SeasonHelper.getSeasonManager().flushMappings();
        }));
        
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
            new DTCommand().registerDTCommand(commandDispatcher);
        }));

//        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new Resources.ReloadListener(event.getServerResources()));

//        @SubscribeEvent
//        public static void addReloadListeners(final AddReloadListenerEvent event) {
//            event.addListener(new Resources.ReloadListener(event.getServerResources()));
//        }
//
//        @SubscribeEvent
//        public static void registerBrewingRecipes(final RegisterBrewingRecipesEvent event) {
//            DendroPotionRecipeHandler.getAllDendroRecipes().forEach(
//                    recipe -> event.getBuilder().addRecipe(recipe));
//        }

    }

}
