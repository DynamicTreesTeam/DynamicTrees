package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.DynamicTreesAddonEntrypoint;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.event.handler.CommonEventHandler;
import com.dtteam.dynamictrees.event.handler.ModEventHandler;
import com.dtteam.dynamictrees.event.handler.VanillaSaplingEventHandler;
import com.dtteam.dynamictrees.platform.FabricMiscHelper;
import com.dtteam.dynamictrees.registry.FabricRegistryHandler;
import com.dtteam.dynamictrees.registry.FabricRegistryLoader;
import com.dtteam.dynamictrees.worldgen.FabricBiomeModifications;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.neoforged.fml.config.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicTreesFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);


        FabricRegistryHandler.setup(DynamicTrees.MOD_ID);

        CommonEventHandler.RegisterEvents();

        runOnEntryPoints(container ->
                container.getEntrypoint().onDynamicTreesPreSetup(), "PreSetup");

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

    public static List<EntrypointContainer<DynamicTreesAddonEntrypoint>> getEntryPointContainers(){
        return FabricLoader.getInstance().getEntrypointContainers(DynamicTrees.MOD_ID, DynamicTreesAddonEntrypoint.class);
    }

    public static void runOnEntryPoints (Consumer<EntrypointContainer<DynamicTreesAddonEntrypoint>> run, String errorMessage){
        for (EntrypointContainer<DynamicTreesAddonEntrypoint> container : DynamicTreesFabric.getEntryPointContainers()) {
            try {
                run.accept(container);
            } catch (Throwable e) {
                DynamicTrees.LOG.error("Failed to invoke {} Entry Point for mod {}: {}", errorMessage, container.getProvider().getMetadata().getId(), e.getMessage());
            }
        }
    }

}
