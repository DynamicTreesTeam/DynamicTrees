package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * An {@link Event} for populating the default database programmatically. This is posted after default populators are
 * read from Json, and before any dimensional populators are read.
 *
 * <p>Fired on the {@link MinecraftForge#EVENT_BUS}.</p>
 *
 * @author Harley O'Connor
 */
public final class PopulateDefaultDatabaseEvent extends Event {

    private final BiomeDatabase defaultDatabase;

    public PopulateDefaultDatabaseEvent(final BiomeDatabase defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    public BiomeDatabase getDefaultDatabase() {
        return defaultDatabase;
    }

}
