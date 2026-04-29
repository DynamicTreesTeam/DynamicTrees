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
    private static final Map<String, Generator<DTDataProvider.Language, String>> extraLangGenerators = new HashMap<>();

    public static void gatherClientData(final String modId, final GatherDataEvent.Client event, Generator<DTDataProvider.Language, String> generator, Registry<?>... registries) {
        addLangGenerator(modId, generator);
        gatherClientData(modId, event, registries);
    }
    public static void gatherClientData(final String modId, final GatherDataEvent.Client event, Registry<?>... registries) {
        gatherLangData(modId, event, registries);
        gatherBlockStateAndModelData(modId, event, registries);
        gatherItemModelData(modId, event, registries);
    }

    public static void gatherServerData(final String modId, final GatherDataEvent.Server event, Registry<?>... registries) {
        gatherTagData(modId, event);
        gatherLootData(modId, event);
    }

    public static void gatherTagData(final String modId, final GatherDataEvent.Server event) {
        final DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        final DTBlockTagsProvider blockTagsProvider = new DTBlockTagsProvider(packOutput, lookupProvider, modId);
        final DTItemTagsProvider itemTagsProvider = new DTItemTagsProvider(packOutput, lookupProvider, modId);

        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, itemTagsProvider);
    }

    public static void gatherBlockStateAndModelData(final String modId, final GatherDataEvent.Client event, Registry<?>... registries) {
        event.getGenerator().addProvider(true,
                new DTSpriteSourceProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), modId, registries));
        event.getGenerator().addProvider(true,
                new DTBlockStateProvider(event.getGenerator().getPackOutput(), modId, Arrays.asList(registries)));
    }

    public static void gatherItemModelData(final String modId, final GatherDataEvent.Client event, Registry<?>... registries) {
        event.getGenerator().addProvider(true,
                new DTItemModelProvider(event.getGenerator().getPackOutput(), modId, Arrays.asList(registries)));
    }

    public static void gatherLootData(final String modId, final GatherDataEvent.Server event) {
        event.getGenerator().addProvider(true,
                new DTLootTableProvider(event.getGenerator().getPackOutput(), modId, event.getLookupProvider()));
    }
    public static void gatherLangData(final String modId, final GatherDataEvent.Client event, Registry<?>... registries){
        event.getGenerator().addProvider(true,
                new DTLangProvider(event.getGenerator().getPackOutput(), modId, Arrays.asList(registries))
        );
    }

    public static void addLangGenerator(String modId, Generator<DTDataProvider.Language, String> generator) {
        GatherDataHelper.extraLangGenerators.put(modId,generator);
    }

    public static Map<String,Generator<DTDataProvider.Language, String>> getExtraLangGenerators() {
        return GatherDataHelper.extraLangGenerators;
    }
}
