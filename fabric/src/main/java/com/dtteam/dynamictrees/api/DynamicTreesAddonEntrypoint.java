package com.dtteam.dynamictrees.api;

import com.dtteam.dynamictrees.registry.FabricRegistryHandler;

public interface DynamicTreesAddonEntrypoint {
    void onDynamicTreesPreSetup();

    static void setupAddon(String modId) {
        FabricRegistryHandler.setup(modId);
    }
}

