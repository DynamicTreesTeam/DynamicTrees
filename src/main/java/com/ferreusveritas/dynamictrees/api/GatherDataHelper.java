package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockTagsProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

/**
 * @author Harley O'Connor
 */
public final class GatherDataHelper {

    public static void gatherAllData(final String modId, final GatherDataEvent event) {
        gatherTagData(modId, event);
        gatherStateAndModelData(modId, event);
    }

    public static void gatherTagData(final String modId, final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();

        final DTBlockTagsProvider blockTagsProvider = new DTBlockTagsProvider(generator, modId, event.getExistingFileHelper());
        final DTItemTagsProvider itemTagsProvider = new DTItemTagsProvider(generator, modId, blockTagsProvider, event.getExistingFileHelper());

        generator.addProvider(blockTagsProvider);
        generator.addProvider(itemTagsProvider);
    }

    public static void gatherStateAndModelData(final String modId, final GatherDataEvent event) {
        event.getGenerator().addProvider(new DTBlockStateProvider(event.getGenerator(), modId, event.getExistingFileHelper()));
    }

}
