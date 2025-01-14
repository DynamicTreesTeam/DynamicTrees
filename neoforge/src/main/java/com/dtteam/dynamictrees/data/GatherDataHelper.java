package com.dtteam.dynamictrees.data;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.provider.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public final class GatherDataHelper {
    private static final Map<String, Generator<DTLangProvider, String>> extraLangGenerators = new HashMap<>();

//    public static void gatherAllData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
//        gatherTagData(modId, event);
//        gatherBlockStateAndModelData(modId, event, registries);
//        gatherItemModelData(modId, event, registries);
//        gatherLootData(modId, event);
//        gatherLangData(modId, event, registries);
//    }
//
//    public static void gatherTagData(final String modId, final GatherDataEvent event) {
//        final DataGenerator generator = event.getGenerator();
//        PackOutput packOutput = generator.getPackOutput();
//        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
//
//        final DTBlockTagsProvider blockTagsProvider = new DTBlockTagsProvider(packOutput, lookupProvider, modId, event.getExistingFileHelper());
//        final DTItemTagsProvider itemTagsProvider = new DTItemTagsProvider(packOutput, modId, lookupProvider, blockTagsProvider.contentsGetter(), event.getExistingFileHelper());
//
//        generator.addProvider(event.includeServer(), blockTagsProvider);
//        generator.addProvider(event.includeServer(), itemTagsProvider);
//    }
//
//    public static void gatherBlockStateAndModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
//        event.getGenerator().addProvider(event.includeServer(), new DTBlockStateProvider(event.getGenerator().getPackOutput(), modId,
//                event.getExistingFileHelper(), Arrays.asList(registries)));
//    }
//
//    public static void gatherItemModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
//        event.getGenerator().addProvider(event.includeServer(), new DTItemModelProvider(event.getGenerator().getPackOutput(), modId,
//                event.getExistingFileHelper(), Arrays.asList(registries)));
//    }
//
//    public static void gatherLootData(final String modId, final GatherDataEvent event) {
//        event.getGenerator().addProvider(event.includeServer(), new DTLootTableProvider(
//                event.getGenerator().getPackOutput(), modId, event.getExistingFileHelper()
//        ));
//    }
//    public static void gatherLangData(final String modId, final GatherDataEvent event, Registry<?>... registries){
//        event.getGenerator().addProvider(event.includeClient(), new DTLangProvider(
//                event.getGenerator().getPackOutput(), modId, Arrays.asList(registries))
//        );
//    }
//
//
//    public static void addLangGenerator(String modId,Generator<DTLangProvider, String> generator) {
//        GatherDataHelper.extraLangGenerators.put(modId,generator);
//    }
    public static Map<String,Generator<DTLangProvider, String>> getExtraLangGenerators() {
        return GatherDataHelper.extraLangGenerators;
    }
}
