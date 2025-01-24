package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.registry.FabricRegistryLoader;
import net.fabricmc.api.ModInitializer;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        DTConfigs.registerConfigs(); //Must be first

        DynamicTrees.init();

        System.out.println("pitopot" + DTConfigs.CONFIG.getOrDefault(IConfigHelper.LEAVES_SEED_DROP_RATE, null));

        FabricRegistryLoader.setup();
    }
}
