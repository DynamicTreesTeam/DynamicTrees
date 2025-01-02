package com.dtteam.dynamictrees.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * This event is posted for add-ons to register custom Json object getters at the right time.
 */
public final class JsonDeserializerRegistryEvent extends Event implements IModBusEvent { }