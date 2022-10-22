package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.command.DTCommand;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onServerStart(final ServerStartingEvent event) {
        SeasonHelper.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

}
