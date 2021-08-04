package com.ferreusveritas.dynamictrees.api.treepacks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Harley O'Connor
 */
public interface IJsonApplierManager {

    void registerAppliers();

    default void fireEvent(final Event event) {
        MinecraftForge.EVENT_BUS.post(event);
    }

}
