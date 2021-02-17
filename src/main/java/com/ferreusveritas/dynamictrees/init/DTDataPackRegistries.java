package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Holds all data pack entries.
 *
 * @author Harley O'Connor
 */
public final class DTDataPackRegistries {

    public static final JoCodeManager JO_CODE_MANAGER = new JoCodeManager();

    @SubscribeEvent
    public static void onAddReloadListeners (final AddReloadListenerEvent event) {
        event.addListener(JO_CODE_MANAGER);
    }

}
