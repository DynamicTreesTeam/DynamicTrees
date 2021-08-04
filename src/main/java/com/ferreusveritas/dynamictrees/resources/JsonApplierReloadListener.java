package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;

import java.util.function.Consumer;

/**
 * An abstract extension of {@link ReloadListener} that stores {@link JsonPropertyApplierList} of type {@link V}.
 *
 * @param <T> The type of {@link Object} returned by {@link ReloadListener#prepare(TreesResourceManager)}.
 * @param <V> The type of {@link Object} the {@link JsonPropertyApplierList} objects are applying to.
 * @author Harley O'Connor
 */
public abstract class JsonApplierReloadListener<T, V> extends ReloadListener<T> {

    protected static final String TYPE = "type";

    protected static final String JSON_EXTENSION = ".json";
    protected static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    protected final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Holds appliers that should be applied both when loading and reloading.
     */
    protected final JsonPropertyApplierList<V> loadReloadAppliers;

    /**
     * Holds appliers that should only be applied when loading.
     */
    protected final JsonPropertyApplierList<V> loadAppliers;

    /**
     * Holds appliers that should only be applied on {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
     * (once all Forge registry entries have been registered).
     */
    protected final JsonPropertyApplierList<V> setupAppliers;

    /**
     * Holds appliers that should only be applied when reloading.
     */
    protected final JsonPropertyApplierList<V> reloadAppliers;

    protected final String applierListIdentifier;

    public JsonApplierReloadListener(final String folderName, final Class<V> objectType, final String applierListIdentifier) {
        super(folderName);

        this.loadReloadAppliers = new JsonPropertyApplierList<>(objectType);
        this.loadAppliers = new JsonPropertyApplierList<>(objectType);
        this.setupAppliers = new JsonPropertyApplierList<>(objectType);
        this.reloadAppliers = new JsonPropertyApplierList<>(objectType);
        this.applierListIdentifier = applierListIdentifier;
    }

    /**
     * Called from {@link DTResourceRegistries#setupTreesResourceManager()}. Sub-classes should can override to register
     * their Json appliers, and should call super so their events are posted properly.
     */
    public void registerAppliers() {
        this.postApplierEvent(this.loadReloadAppliers, this.applierListIdentifier);
        this.postApplierEvent(this.loadAppliers, this.applierListIdentifier + JsonApplierRegistryEvent.LOAD_SUFFIX);
        this.postApplierEvent(this.setupAppliers, this.applierListIdentifier + JsonApplierRegistryEvent.SETUP_SUFFIX);
        this.postApplierEvent(this.reloadAppliers, this.applierListIdentifier + JsonApplierRegistryEvent.RELOAD_SUFFIX);
    }

    /**
     * Creates a {@link JsonApplierRegistryEvent} instance for the given {@link JsonPropertyApplierList} and identifier,
     * posting it to the mod event bus from {@link ModLoader}.
     *
     * @param applierList           The {@link JsonPropertyApplierList} to post an applier event for.
     * @param applierListIdentifier The identifier for the applier list.
     */
    protected void postApplierEvent(final JsonPropertyApplierList<?> applierList, final String applierListIdentifier) {
        ModLoader.get().postEvent(new JsonApplierRegistryEvent<>(applierList, applierListIdentifier));
    }

    /**
     * Checks if the entry for the given {@link JsonObject} should load based on the {@link ModList}. This allows
     * entries to only load if the given mod ID is loaded, which can be used by add-ons to create custom species types
     * if, for example, dynamic trees plus is installed.
     *
     * @param jsonObject    The {@link JsonObject} to check.
     * @param errorConsumer The {@link Consumer<String>} to accept if there is an error.
     * @return Whether or not the given entry should load.
     */
    protected boolean shouldLoad(final JsonObject jsonObject, final Consumer<String> errorConsumer) {
        return ModList.get().isLoaded(JsonHelper.getOrDefault(jsonObject, "only_if_loaded",
                String.class, DynamicTrees.MOD_ID, errorConsumer));
    }

    /**
     * Gets the {@link #loadReloadAppliers} for this {@link JsonApplierReloadListener} object.
     *
     * @return The {@link #loadReloadAppliers} for this {@link JsonApplierReloadListener} object.
     */
    public JsonPropertyApplierList<V> getLoadReloadAppliers() {
        return this.loadReloadAppliers;
    }

    /**
     * Gets the {@link #loadAppliers} for this {@link JsonApplierReloadListener} object.
     *
     * @return The {@link #loadAppliers} for this {@link JsonApplierReloadListener} object.
     */
    public JsonPropertyApplierList<V> getLoadAppliers() {
        return this.loadAppliers;
    }

    /**
     * Gets the {@link #setupAppliers} for this {@link JsonApplierReloadListener} object.
     *
     * @return The {@link #setupAppliers} for this {@link JsonApplierReloadListener} object.
     */
    public JsonPropertyApplierList<V> getSetupAppliers() {
        return this.setupAppliers;
    }

    /**
     * Gets the {@link #reloadAppliers} for this {@link JsonApplierReloadListener} object.
     *
     * @return The {@link #reloadAppliers} for this {@link JsonApplierReloadListener} object.
     */
    public JsonPropertyApplierList<V> getReloadAppliers() {
        return this.reloadAppliers;
    }

}
