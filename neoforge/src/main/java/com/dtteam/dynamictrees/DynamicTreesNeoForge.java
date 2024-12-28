package com.dtteam.dynamictrees;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(DynamicTreesCommon.MOD_ID)
public class DynamicTreesNeoForge {

    public DynamicTreesNeoForge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        DynamicTreesCommon.LOG.info("Hello NeoForge world!");
        DynamicTreesCommon.init();

    }
}