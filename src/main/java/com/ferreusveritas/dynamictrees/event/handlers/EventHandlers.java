package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/**
 * Holds and manages event handlers.
 *
 * @author Harley O'Connor
 */
public final class EventHandlers {

    public static final CommonEventHandler COMMON_EVENT_HANDLER = new CommonEventHandler();
    public static final ServerEventHandler SERVER_EVENT_HANDLER = new ServerEventHandler();

    public static final LeafUpdateEventHandler LEAF_UPDATE_EVENT_HANDLER = new LeafUpdateEventHandler();

    public static final VanillaSaplingEventHandler VANILLA_SAPLING_EVENT_HANDLER = new VanillaSaplingEventHandler();
    public static final PoissonDiscEventHandler POISSON_DISC_EVENT_HANDLER = new PoissonDiscEventHandler();

    /**
     * Registers common events, called in {@link DynamicTrees#DynamicTrees()}.
     */
    public static void registerCommon() {
        MinecraftForge.EVENT_BUS.register(COMMON_EVENT_HANDLER);
        MinecraftForge.EVENT_BUS.register(SERVER_EVENT_HANDLER);

        if (ModList.get().isLoaded(DynamicTrees.FAST_LEAF_DECAY)) {
            MinecraftForge.EVENT_BUS.register(LEAF_UPDATE_EVENT_HANDLER);
        }
    }

    /**
     * Registers or unregisters event handlers based on config changes. Called when the config is loaded or reloaded in
     * {@link DTConfigs}.
     */
    public static void configReload() {
        registerOrUnregister(VANILLA_SAPLING_EVENT_HANDLER, DTConfigs.REPLACE_VANILLA_SAPLING.get());
        registerOrUnregister(POISSON_DISC_EVENT_HANDLER, DTConfigs.WORLD_GEN.get());
    }

    /**
     * Registers or unregisters the given object to the {@link MinecraftForge#EVENT_BUS}, depending on the boolean
     * given.
     *
     * @param handler  The handler object to register/unregisters.
     * @param register True if handler should be registered.
     */
    private static void registerOrUnregister(final Object handler, final boolean register) {
        if (register) {
            MinecraftForge.EVENT_BUS.register(handler);
        } else {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
    }

}
