package com.dtteam.dynamictrees.data;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.data.provider.DTBlockTagsProvider;
import com.dtteam.dynamictrees.data.provider.DTDatapackBuiltinEntriesProvider;
import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
import com.dtteam.dynamictrees.data.provider.DTItemTagsProvider;
import com.dtteam.dynamictrees.data.provider.DTLangProvider;
import com.dtteam.dynamictrees.data.provider.DTLootTableProvider;
import com.dtteam.dynamictrees.data.provider.DTSpriteSourceProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class GatherDataHelper {
    private static final Map<String, Generator<DTDataProvider.Language, String>> extraLangGenerators = new HashMap<>();

    public static void gatherServerData(final String modId, final GatherDataEvent.Server event) {
        event.createDatapackRegistryObjects(DTDatapackBuiltinEntriesProvider.registries());
        gatherTagData(modId, event);
        gatherLootData(modId, event);
    }

    public static void gatherClientData(final String modId, final GatherDataEvent.Client event,
                                        Generator<DTDataProvider.Language, String> generator, Registry<?>... registries) {
        addLangGenerator(modId, generator);
        gatherLangData(modId, event, registries);
        event.createProvider(output -> new DTBlockStateProvider(output, modId, Arrays.asList(registries)));
        event.createProvider(output -> new DTItemModelProvider(output, modId, Arrays.asList(registries)));
        event.createProvider((output, lookup) -> new DTSpriteSourceProvider(output, lookup, modId, registries));
    }

    public static void gatherTagData(final String modId, final GatherDataEvent.Server event) {
        event.createProvider((output, lookup) -> new DTBlockTagsProvider(output, modId, lookup));
        event.createProvider((output, lookup) -> new DTItemTagsProvider(output, modId, lookup));
    }

    public static void gatherLootData(final String modId, final GatherDataEvent.Server event) {
        event.createProvider((output, lookup) -> new DTLootTableProvider(output, modId, lookup));
    }

    public static void gatherLangData(final String modId, final GatherDataEvent.Client event, Registry<?>... registries) {
        event.createProvider(output -> new DTLangProvider(output, modId, Arrays.asList(registries)));
    }

    public static void addLangGenerator(String modId, Generator<DTDataProvider.Language, String> generator) {
        GatherDataHelper.extraLangGenerators.put(modId, generator);
    }

    public static Map<String, Generator<DTDataProvider.Language, String>> getExtraLangGenerators() {
        return GatherDataHelper.extraLangGenerators;
    }
}
