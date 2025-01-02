package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onServerStart(final ServerStartingEvent event) {
        SeasonHelper.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event) {
//        new DTCommand().registerDTCommand(event.getDispatcher());
    }

}
