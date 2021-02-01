package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.command.DTCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onServerStart (final FMLServerStartingEvent event) {
        // Register DT command.
        new DTCommand().registerDTCommand(event.getServer().getCommandManager().getDispatcher());
    }

}
