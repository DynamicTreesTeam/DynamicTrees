package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.api.datapacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author Harley O'Connor
 */
public abstract class JsonApplierReloadListener<T, V> extends ReloadListener<T> {

    protected static final String JSON_EXTENSION = ".json";
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    protected final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    /** Holds appliers that should be applied both when loading and reloading. */
    protected final JsonPropertyApplierList<V> appliers;

    /** Holds appliers that should only be applied when loading. */
    protected final JsonPropertyApplierList<V> loadAppliers;

    /** Holds appliers that should only be applied when reloading. */
    protected final JsonPropertyApplierList<V> reloadAppliers;

    public JsonApplierReloadListener(final String folderName, final Class<V> objectType, final String applierRegistryName) {
        super(folderName);

        this.appliers = new JsonPropertyApplierList<>(objectType);
        this.loadAppliers = new JsonPropertyApplierList<>(objectType);
        this.reloadAppliers = new JsonPropertyApplierList<>(objectType);

        this.registerAppliers(applierRegistryName);
    }

    /**
     * Called from {@link JsonReloadListener#JsonReloadListener(String, Class, String)}. Sub-classes should
     * can override to register their Json appliers.
     */
    public void registerAppliers(final String applierRegistryName) {
        MinecraftForge.EVENT_BUS.post(new JsonApplierRegistryEvent<>(this.appliers, applierRegistryName));
        MinecraftForge.EVENT_BUS.post(new JsonApplierRegistryEvent<>(this.loadAppliers, applierRegistryName + "_load"));
        MinecraftForge.EVENT_BUS.post(new JsonApplierRegistryEvent<>(this.reloadAppliers, applierRegistryName + "_reload"));
    }

}
