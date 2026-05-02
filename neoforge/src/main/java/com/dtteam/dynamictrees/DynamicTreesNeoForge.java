package com.dtteam.dynamictrees;


import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.data.generator.DTExtraLangGenerator;
import com.dtteam.dynamictrees.data.generator.DTExtraModelGenerator;
import com.dtteam.dynamictrees.data.generator.DataGenerators;
import com.dtteam.dynamictrees.event.handler.OptionalHandlers;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resources;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(DynamicTrees.MOD_ID)
public class DynamicTreesNeoForge {

    public DynamicTreesNeoForge(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::gatherServerData);
        eventBus.addListener(this::gatherClientData);

        container.registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        container.registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

        NeoForgeRegistryHandler.setup(DynamicTrees.MOD_ID, eventBus);

        DynamicTrees.init();

        NeoForgeRegistryLoader.setup(eventBus);

        OptionalHandlers.registerHandlers();

        DataGenerators.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        BlockColorMultipliers.cleanUp();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        DynamicTrees.commonSetup();
    }

    private void gatherServerData(final GatherDataEvent.Server event) {
//        //Generate the feature replacement data
//        DataGenerator dataGen = event.getGenerator();
//        dataGen.addProvider(true, new DTDatapackBuiltinEntriesProvider(
//                dataGen.getPackOutput(), event.getLookupProvider(), Set.of(DynamicTrees.MOD_ID, DynamicTrees.MINECRAFT)
//        ));
    }

    private void gatherClientData(final GatherDataEvent.Client event) {
        Resources.MANAGER.gatherData();
        GatherDataHelper.addModelGenerator(DynamicTrees.MOD_ID, new DTExtraModelGenerator());
        GatherDataHelper.addLangGenerator(DynamicTrees.MOD_ID, new DTExtraLangGenerator());
        GatherDataHelper.gatherClientData(
                DynamicTrees.MOD_ID, event,
                SoilProperties.REGISTRY,
                Family.REGISTRY,
                Species.REGISTRY,
                LeavesProperties.REGISTRY
        );
    }

}