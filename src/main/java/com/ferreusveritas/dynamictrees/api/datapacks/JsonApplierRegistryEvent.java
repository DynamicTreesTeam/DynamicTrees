package com.ferreusveritas.dynamictrees.api.datapacks;

import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public class JsonApplierRegistryEvent<T> extends Event implements IModBusEvent {

    public static final String SPECIES = "species";
    public static final String FAMILY = "family";
    public static final String LEAVES_PROPERTIES = "leaves_properties";

    public static final String LOAD_SUFFIX = "_load";
    public static final String RELOAD_SUFFIX = "_reload";

    public final JsonPropertyApplierList<T> applierList;
    private final String applierListIdentifier;

    public JsonApplierRegistryEvent(JsonPropertyApplierList<T> applierList, String applierListIdentifier) {
        this.applierList = applierList;
        this.applierListIdentifier = applierListIdentifier;
    }

    public JsonPropertyApplierList<T> getApplierList() {
        return applierList;
    }

    public String getApplierListIdentifier() {
        return applierListIdentifier;
    }

    public boolean isLoadApplier () {
        return this.applierListIdentifier.endsWith(LOAD_SUFFIX);
    }

    public boolean isReloadApplier () {
        return this.applierListIdentifier.endsWith(RELOAD_SUFFIX);
    }

}
