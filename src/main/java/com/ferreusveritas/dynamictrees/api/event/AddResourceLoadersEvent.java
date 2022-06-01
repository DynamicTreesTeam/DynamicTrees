package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.resource.ResourceManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public final class AddResourceLoadersEvent extends Event implements IModBusEvent {

    private final ResourceManager resourceManager;

    public AddResourceLoadersEvent(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

}
