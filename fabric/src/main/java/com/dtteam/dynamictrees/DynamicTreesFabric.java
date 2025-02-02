package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.event.handler.CommonEventHandler;
import com.dtteam.dynamictrees.registry.FabricRegistryLoader;
import net.fabricmc.api.ModInitializer;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        DTConfigs.registerConfigs(); //Must be first

        CommonEventHandler.RegisterEvents();

        DynamicTrees.init();

        FabricRegistryLoader.setup();
    }

}
