package com.ferreusveritas.dynamictrees.util;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class CommonSetup {

    private static final List<Consumer<FMLCommonSetupEvent>> SETUP_HANDLERS = new ArrayList<>();

    public static void runOnCommonSetup(Consumer<FMLCommonSetupEvent> handler) {
        SETUP_HANDLERS.add(handler);
    }

    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        SETUP_HANDLERS.forEach(consumer -> consumer.accept(event));
    }

}
