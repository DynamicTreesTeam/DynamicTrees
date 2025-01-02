package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.config.FabricRegistryLoader;
import net.fabricmc.api.ModInitializer;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        FabricRegistryLoader.setup();

    }
}
