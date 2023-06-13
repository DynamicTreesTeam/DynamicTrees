package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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

    public static final WorldGenEventHandler WORLD_GEN_EVENT_HANDLER = new WorldGenEventHandler();

    /**
     * Registers common events, called in {@link DynamicTrees#DynamicTrees()}.
     */
    public static void registerCommon() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.register(COMMON_EVENT_HANDLER);
        bus.register(SERVER_EVENT_HANDLER);

        if (ModList.get().isLoaded(DynamicTrees.FAST_LEAF_DECAY)) {
            bus.register(LEAF_UPDATE_EVENT_HANDLER);
        }

        bus.register(WORLD_GEN_EVENT_HANDLER);
    }

    /**
     * Registers or unregisters event handlers based on config changes. Called when the config is loaded or reloaded in
     * {@link DTConfigs}.
     */
    public static void configReload() {
        registerOrUnregister(VANILLA_SAPLING_EVENT_HANDLER, DTConfigs.REPLACE_VANILLA_SAPLING.get());
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
