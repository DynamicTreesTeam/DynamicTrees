package com.dtteam.dynamictrees;


import com.dtteam.dynamictrees.init.DTClient;
import com.dtteam.dynamictrees.init.DTRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@Mod(DynamicTreesCommon.MOD_ID)
public class DynamicTreesNeoForge {

    public DynamicTreesNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::gatherData);

        DynamicTreesCommon.init();

        DTRegistries.setup(eventBus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTClient.setup();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
//        DTLoot.load();
//        DynamicTreeFeature.setup();
//
//        // Clears and locks registry handlers to free them from memory.
//        RegistryHandler.REGISTRY.clear();
//
//        DTRegistries.DENDRO_POTION.get().registerRecipes();
//
//        Resources.MANAGER.setup();
    }

    private void gatherData(final GatherDataEvent event) {
        //Generate the tree block and item data
//        Resources.MANAGER.gatherData();
//        GatherDataHelper.addLangGenerator(MOD_ID, new DTExtraLangGenerator());
//        GatherDataHelper.gatherAllData(
//                MOD_ID,
//                event,
//                SoilProperties.REGISTRY,
//                Family.REGISTRY,
//                Species.REGISTRY,
//                LeavesProperties.REGISTRY
//        );
//        //Generate the feature replacement data
//        DataGenerator dataGen = event.getGenerator();
//        dataGen.addProvider(event.includeServer(), new DTDatapackBuiltinEntriesProvider(
//                dataGen.getPackOutput(), event.getLookupProvider(), Set.of(DynamicTrees.MOD_ID, DynamicTrees.MINECRAFT)
//        ));
    }

}