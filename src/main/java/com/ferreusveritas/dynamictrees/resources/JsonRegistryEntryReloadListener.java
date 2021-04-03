package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

/**
 * An abstract extension of {@link JsonReloadListener} implementing {@link #apply(Map, IResourceManager, boolean)}
 * for {@link RegistryEntry} (and {@link IResettable}) objects to be loaded, setup, and registered from Json.
 *
 * @param <V> The type of the {@link RegistryEntry}.
 * @author Harley O'Connor
 */
public abstract class JsonRegistryEntryReloadListener<V extends RegistryEntry<V> & IResettable<V>> extends JsonReloadListener<V> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TypedRegistry<V> registry;
    private final String registryName;

    public JsonRegistryEntryReloadListener(final TypedRegistry<V> registry, final String folderName) {
        this(registry, folderName, folderName);
    }

    public JsonRegistryEntryReloadListener(final TypedRegistry<V> registry, final String folderName, final String applierRegistryName) {
        super(folderName, registry.getType(), applierRegistryName);

        this.registry = registry;
        this.registryName = registry.getName();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> preparedObject, IResourceManager resourceManager, boolean firstLoad) {
        this.registry.unlock();

        preparedObject.forEach((registryName, jsonElement) -> {
            if (!jsonElement.isJsonObject()) {
                LOGGER.warn("Skipping loading " + this.registryName + " '{}' as its root element is not a Json object.", registryName);
                return;
            }

            final JsonObject jsonObject = TypedRegistry.putJsonRegistryName(jsonElement.getAsJsonObject(), registryName);

            // Skip the current entry if it shouldn't load.
            if (!this.shouldLoad(jsonObject, "Error loading data for " + this.registryName + " '" + registryName + "': "))
                return;

            final boolean newEntry = !this.registry.has(registryName);
            final V registryEntry;

            final Consumer<String> errorConsumer = error -> LOGGER.error("Error whilst loading {} '{}': {}", this.registryName, registryName, error);
            final Consumer<String> warningConsumer = warning -> LOGGER.warn("Warning whilst loading {} '{}': {}", this.registryName, registryName, warning);

            if (newEntry) {
                registryEntry = this.registry.getType(jsonObject, registryName).decode(jsonObject);

                // Stop loading this entry (error should have been logged already).
                if (registryEntry == null)
                    return;

                if (firstLoad) {
                    this.preLoad(jsonObject, registryEntry, errorConsumer, warningConsumer);
                    this.loadAppliers.applyAll(jsonObject, registryEntry).forEachErrorWarning(errorConsumer, warningConsumer);
                } else registryEntry.setPreReloadDefaults();
            } else {
                registryEntry = this.registry.get(registryName).reset().setPreReloadDefaults();
            }

            if (!firstLoad)
                this.reloadAppliers.applyAll(jsonObject, registryEntry).forEachErrorWarning(errorConsumer, warningConsumer);

            this.appliers.applyAll(jsonObject, registryEntry).forEachError(errorConsumer).forEachErrorWarning(errorConsumer, warningConsumer);

            if (!firstLoad)
                registryEntry.setPostReloadDefaults();

            if (newEntry) {
                this.postLoad(jsonObject, registryEntry, errorConsumer, warningConsumer);

                this.registry.register(registryEntry);
                LOGGER.debug("Loaded and registered {}: {}.", this.registryName, registryEntry.toLoadDataString());
            } else {
                LOGGER.debug("Loaded {} data: {}.", this.registryName, registryEntry.toReloadDataString());
            }
        });

        if (!firstLoad)
            this.registry.lock();
    }

    /**
     * Called directly before {@link #loadAppliers} are applied on initial load.
     *
     * @param jsonObject The {@link JsonObject} for the {@code registryEntry}.
     * @param registryEntry The current {@link RegistryEntry<V>}.
     * @param errorConsumer The {@link Consumer} for error messages.
     * @param warningConsumer The {@link Consumer} for warnings.
     */
    protected void preLoad(final JsonObject jsonObject, final V registryEntry, final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) { }

    /**
     * Called after the {@code registryEntry} has been loaded for the first time (but
     * before it has been registered).
     *
     * @param jsonObject The {@link JsonObject} for the {@code registryEntry}.
     * @param registryEntry The current {@link RegistryEntry<V>}.
     * @param errorConsumer The {@link Consumer} for error messages.
     * @param warningConsumer The {@link Consumer} for warnings.
     */
    protected void postLoad(final JsonObject jsonObject, final V registryEntry, final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) { }

}
