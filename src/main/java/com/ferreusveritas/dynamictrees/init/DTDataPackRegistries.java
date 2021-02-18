package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Holds and registers data pack entries ({@link IFutureReloadListener} objects).
 *
 * @author Harley O'Connor
 */
public final class DTDataPackRegistries {

    public static final JoCodeManager JO_CODE_MANAGER = new JoCodeManager();
    public static final BiomeDatabaseManager BIOME_DATABASE_MANAGER = new BiomeDatabaseManager();

    @SubscribeEvent
    public static void onAddReloadListeners (final AddReloadListenerEvent event) {
        event.addListener(JO_CODE_MANAGER);
        event.addListener(BIOME_DATABASE_MANAGER);
    }

}
