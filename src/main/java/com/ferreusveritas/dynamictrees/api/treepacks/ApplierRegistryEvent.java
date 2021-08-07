package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyApplierList;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public class ApplierRegistryEvent<O> extends GenericEvent<O> implements IModBusEvent {

    public static final String SPECIES = "species";
    public static final String FAMILY = "family";
    public static final String LEAVES_PROPERTIES = "leaves_properties";
    public static final String GLOBAL_DROP_CREATORS = "global_drop_creators";
    public static final String SOIL_PROPERTIES = "soil_properties";

    public final JsonPropertyApplierList<O> applierList;
    private final String identifier;

    public ApplierRegistryEvent(JsonPropertyApplierList<O> applierList, String identifier) {
        super(applierList.getObjectType());

        this.applierList = applierList;
        this.identifier = identifier;
    }

    public static class Load<O> extends ApplierRegistryEvent<O> {
        public Load(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    public static class Setup<O> extends ApplierRegistryEvent<O> {
        public Setup(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    public static class Reload<O> extends ApplierRegistryEvent<O> {
        public Reload(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    public static class Common<O> extends ApplierRegistryEvent<O> {
        public Common(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    public JsonPropertyApplierList<O> getApplierList() {
        return applierList;
    }

    public String getIdentifier() {
        return identifier;
    }

}
