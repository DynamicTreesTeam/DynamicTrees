package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.*;
import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.event.handler.*;
import com.dtteam.dynamictrees.platform.*;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.worldgen.*;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.entrypoint.*;
import net.neoforged.fml.config.*;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID,ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID,ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);


        FabricRegistryHandler.setup(DynamicTrees.MOD_ID);

        CommonEventHandler.RegisterEvents();

        for (EntrypointContainer<DynamicTreesAddonEntrypoint> container : FabricLoader.getInstance().getEntrypointContainers("dynamictrees", DynamicTreesAddonEntrypoint.class)) {
            try {
                container.getEntrypoint().onDynamicTreesPreSetup();
            } catch (Throwable e) {
                DynamicTrees.LOG.error("Failed to invoke Dynamic Trees addon entrypoint for mod: {}", container.getProvider().getMetadata().getId(), e);
            }
        }

        ModEventHandler.RegisterEvents();
        VanillaSaplingEventHandler.register();

        DynamicTrees.init();

        FabricRegistryLoader.setup();

        DynamicTrees.commonSetup();

        FabricBiomeModifications.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            VanillaSaplingEventHandler.updateEnabled();
            FabricMiscHelper.debugSpeciesRegistry();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            FabricMiscHelper.currentServer = null;
        });


    }

}
