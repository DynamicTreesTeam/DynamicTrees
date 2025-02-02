package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

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

    }

}
