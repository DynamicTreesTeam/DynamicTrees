package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.init.DTRegistries;
import net.fabricmc.api.ModInitializer;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        DTRegistries.setup();

    }
}
