package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.DynamicTreesNeoForge;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Holds and manages event handlers.
 *
 * @author Harley O'Connor
 */
public final class EventHandlers {

    //Common Bus Events
    public static final CommonEventHandler COMMON_EVENT_HANDLER = new CommonEventHandler();
    public static final ServerEventHandler SERVER_EVENT_HANDLER = new ServerEventHandler();
    public static final LeafUpdateEventHandler LEAF_UPDATE_EVENT_HANDLER = new LeafUpdateEventHandler();
    public static final VanillaSaplingEventHandler VANILLA_SAPLING_EVENT_HANDLER = new VanillaSaplingEventHandler();
    public static final WorldGenEventHandler WORLD_GEN_EVENT_HANDLER = new WorldGenEventHandler();

    //Mod Bus Events
    public static final BakedModelEventHandler BAKED_MODEL_EVENT_HANDLER = new BakedModelEventHandler();

    /**
     * Registers common events, called in {@link DynamicTrees}.
     */
    public static void registerHandlers() {
        IEventBus bus = NeoForge.EVENT_BUS;
        IEventBus modBus = DynamicTreesNeoForge.MOD_EVENT_BUS;

        bus.register(COMMON_EVENT_HANDLER);
        bus.register(SERVER_EVENT_HANDLER);
        bus.register(WORLD_GEN_EVENT_HANDLER);
        if (Services.PLATFORM.isModLoaded(DynamicTrees.FAST_LEAF_DECAY)) {
            bus.register(LEAF_UPDATE_EVENT_HANDLER);
        }

        modBus.register(BAKED_MODEL_EVENT_HANDLER);
    }

    /**
     * Registers or unregisters event handlers based on config changes. Called when the config is loaded or reloaded in
     * {@link DTConfigs}.
     */
    public static void configReload() {
        registerOrUnregister(VANILLA_SAPLING_EVENT_HANDLER, Services.CONFIG.getBoolConfig("replaceVanillaSaplings"));
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
            NeoForge.EVENT_BUS.register(handler);
        } else {
            NeoForge.EVENT_BUS.unregister(handler);
        }
    }

}
