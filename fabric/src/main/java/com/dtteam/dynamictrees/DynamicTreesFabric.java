package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.event.handler.CommonEventHandler;
import com.dtteam.dynamictrees.event.handler.ModEventHandler;
import com.dtteam.dynamictrees.event.handler.VanillaSaplingEventHandler;
import com.dtteam.dynamictrees.platform.FabricMiscHelper;
import com.dtteam.dynamictrees.registry.FabricRegistryHandler;
import com.dtteam.dynamictrees.registry.FabricRegistryLoader;
import com.dtteam.dynamictrees.worldgen.FabricBiomeModifications;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.*;

public class DynamicTreesFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        NeoForgeConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID,ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        NeoForgeConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID,ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);


        FabricRegistryHandler.setup(DynamicTrees.MOD_ID);

        CommonEventHandler.RegisterEvents();
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

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                LeavesProperties.postInitClient();
                BlockColorMultipliers.cleanUp();
                DynamicTreesFabricClient.registerBlockColors();
                DynamicTreesFabricClient.discoverWoodColors();
            });
        }
    }

}
