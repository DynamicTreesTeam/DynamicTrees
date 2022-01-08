package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockTagsProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.Arrays;

/**
 * @author Harley O'Connor
 */
public final class GatherDataHelper {

    public static void gatherAllData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        gatherTagData(modId, event);
        gatherBlockStateAndModelData(modId, event, registries);
        gatherItemModelData(modId, event, registries);
    }

    public static void gatherTagData(final String modId, final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();

        final DTBlockTagsProvider blockTagsProvider = new DTBlockTagsProvider(generator, modId, event.getExistingFileHelper());
        final DTItemTagsProvider itemTagsProvider = new DTItemTagsProvider(generator, modId, blockTagsProvider, event.getExistingFileHelper());

        generator.addProvider(blockTagsProvider);
        generator.addProvider(itemTagsProvider);
    }

    public static void gatherBlockStateAndModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        event.getGenerator().addProvider(new DTBlockStateProvider(event.getGenerator(), modId,
                event.getExistingFileHelper(), Arrays.asList(registries)));
    }

    public static void gatherItemModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        event.getGenerator().addProvider(new DTItemModelProvider(event.getGenerator(), modId,
                event.getExistingFileHelper(), Arrays.asList(registries)));
    }

}
