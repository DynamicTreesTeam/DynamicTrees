package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Holds and manages optional event handlers.
 * These handlers are not always active and depend on some condition.
 *
 * @author Harley O'Connor
 */
public final class OptionalHandlers {

    public static final LeafUpdateEventHandler LEAF_UPDATE_EVENT_HANDLER = new LeafUpdateEventHandler();
    public static final VanillaSaplingEventHandler VANILLA_SAPLING_EVENT_HANDLER = new VanillaSaplingEventHandler();

    /**
     * Registers common events, called in {@link DynamicTrees}.
     */
    public static void registerHandlers() {
        IEventBus bus = NeoForge.EVENT_BUS;

        if (Services.PLATFORM.isModLoaded(DynamicTrees.FAST_LEAF_DECAY)) {
            bus.register(LEAF_UPDATE_EVENT_HANDLER);
        }
    }

    /**
     * Registers or unregisters event handlers based on config changes. Called when the config is loaded or reloaded in
     * {@link DTConfigs}.
     */
    public static void configReload() {
        registerOrUnregister(VANILLA_SAPLING_EVENT_HANDLER, Services.CONFIG.getBoolConfig(IConfigHelper.REPLACE_VANILLA_SAPLINGS));
    }

    /**
     * Registers or unregisters the given object to the {@link NeoForge#EVENT_BUS}, depending on the boolean
     * given.
     *
     * @param handler  The handler object to register/unregisters.
     * @param register True if handler should be registered.
     */
    private static void registerOrUnregister(final Object handler, final boolean register) {
        if (register) {
            NeoForge.EVENT_BUS.register(handler);
        } else {
            NeoForge.EVENT_BUS.unregister(handler);
        }
    }

}
