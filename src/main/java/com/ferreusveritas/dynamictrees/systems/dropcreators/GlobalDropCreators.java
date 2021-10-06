package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class GlobalDropCreators {

    private GlobalDropCreators() {}

    private static final Map<ResourceLocation, ConfiguredDropCreator> ENTRIES = Maps.newHashMap();

    public static List<ConfiguredDropCreator> getAll() {
        return Lists.newLinkedList(ENTRIES.values());
    }

    public static <C extends DropContext> void appendAll(final DropCreator.Type<C> type, final C context) {
        getAll().forEach(configuration -> configuration.appendDrops(type, context));
    }

    public static ConfiguredDropCreator get(final ResourceLocation registryName) {
        return ENTRIES.get(registryName);
    }

    public static void put(final ResourceLocation registryName, final ConfiguredDropCreator configuredDropCreator) {
        ENTRIES.put(registryName, configuredDropCreator);
    }

}
