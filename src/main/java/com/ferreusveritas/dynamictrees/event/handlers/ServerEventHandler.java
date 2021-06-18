package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.command.DTCommand;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onServerStart (final FMLServerStartingEvent event) {
        SeasonHelper.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

}
