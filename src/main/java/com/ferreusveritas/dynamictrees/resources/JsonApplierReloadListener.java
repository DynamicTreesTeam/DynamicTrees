package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.fml.ModLoader;

/**
 * An abstract extension of {@link ReloadListener} that stores {@link JsonPropertyApplierList}
 * of type {@link V}.
 *
 * @param <T> The type of {@link Object} returned by {@link #prepare(IResourceManager)}.
 * @param <V> The type of {@link Object} the {@link JsonPropertyApplierList} objects are applying to.
 * @author Harley O'Connor
 */
public abstract class JsonApplierReloadListener<T, V> extends ReloadListener<T> {

    protected static final String TYPE = "type";

    protected static final String JSON_EXTENSION = ".json";
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    protected final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    /** Holds appliers that should be applied both when loading and reloading. */
    protected final JsonPropertyApplierList<V> appliers;

    /** Holds appliers that should only be applied when loading. */
    protected final JsonPropertyApplierList<V> loadAppliers;

    /** Holds appliers that should only be applied when reloading. */
    protected final JsonPropertyApplierList<V> reloadAppliers;

    public JsonApplierReloadListener(final String folderName, final Class<V> objectType, final String applierListIdentifier) {
        super(folderName);

        this.appliers = new JsonPropertyApplierList<>(objectType);
        this.loadAppliers = new JsonPropertyApplierList<>(objectType);
        this.reloadAppliers = new JsonPropertyApplierList<>(objectType);

        this.registerAppliers(applierListIdentifier);
    }

    /**
     * Called from {@link JsonReloadListener#JsonReloadListener(String, Class, String)}. Sub-classes should
     * can override to register their Json appliers.
     *
     * @param applierListIdentifier The identifier for the applier lists.
     */
    public void registerAppliers(final String applierListIdentifier) {
        this.postApplierEvent(this.appliers, applierListIdentifier);
        this.postApplierEvent(this.loadAppliers, applierListIdentifier + JsonApplierRegistryEvent.LOAD_SUFFIX);
        this.postApplierEvent(this.reloadAppliers, applierListIdentifier + JsonApplierRegistryEvent.RELOAD_SUFFIX);
    }

    /**
     * Creates a {@link JsonApplierRegistryEvent} instance for the given {@link JsonPropertyApplierList}
     * and identifier, posting it to the mod event bus from {@link ModLoader}.
     *
     * @param applierList The {@link JsonPropertyApplierList} to post an applier event for.
     * @param applierListIdentifier The identifier for the applier list.
     */
    protected void postApplierEvent (final JsonPropertyApplierList<?> applierList, final String applierListIdentifier) {
        ModLoader.get().postEvent(new JsonApplierRegistryEvent<>(applierList, applierListIdentifier));
    }

    public JsonPropertyApplierList<V> getAppliers() {
        return appliers;
    }

    public JsonPropertyApplierList<V> getLoadAppliers() {
        return loadAppliers;
    }

    public JsonPropertyApplierList<V> getReloadAppliers() {
        return reloadAppliers;
    }

}
