package com.dtteam.dynamictrees;


import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.data.generator.DTExtraLangGenerator;
import com.dtteam.dynamictrees.data.provider.DTDatapackBuiltinEntriesProvider;
import com.dtteam.dynamictrees.event.handler.OptionalHandlers;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resources;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@Mod(DynamicTrees.MOD_ID)
public class DynamicTreesNeoForge {

    public static IEventBus MOD_EVENT_BUS;

    public DynamicTreesNeoForge(IEventBus eventBus, ModContainer container) {
        MOD_EVENT_BUS = eventBus;

        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::gatherData);

        container.registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        container.registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

        DynamicTrees.init();

        NeoForgeRegistryLoader.setup(eventBus);

        OptionalHandlers.registerHandlers();

        //Do not use the mod event bus outside the constructor.
        MOD_EVENT_BUS = null;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LeavesProperties.postInitClient();
        BlockColorMultipliers.cleanUp();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        DynamicTrees.commonSetup();
    }

    private void gatherData(final GatherDataEvent event) {
        //Generate the tree block and item data
        Resources.MANAGER.gatherData();
        GatherDataHelper.gatherAllData(
                DynamicTrees.MOD_ID, event,
                new DTExtraLangGenerator(),
                SoilProperties.REGISTRY,
                Family.REGISTRY,
                Species.REGISTRY,
                LeavesProperties.REGISTRY
        );
        //Generate the feature replacement data
        DataGenerator dataGen = event.getGenerator();
        dataGen.addProvider(event.includeServer(), new DTDatapackBuiltinEntriesProvider(
                dataGen.getPackOutput(), event.getLookupProvider(), Set.of(DynamicTrees.MOD_ID, DynamicTrees.MINECRAFT)
        ));
    }

}