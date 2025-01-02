package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public final class AddResourceLoadersEvent extends Event implements IModBusEvent {

    private final TreeResourceManager resourceManager;

    public AddResourceLoadersEvent(TreeResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public TreeResourceManager getResourceManager() {
        return resourceManager;
    }

}
