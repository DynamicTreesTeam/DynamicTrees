package com.ferreusveritas.dynamictrees.api.datapacks;

import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Harley O'Connor
 */
public class JsonApplierRegistryEvent<T> extends Event {

    public static final String SPECIES = "species";

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

}
