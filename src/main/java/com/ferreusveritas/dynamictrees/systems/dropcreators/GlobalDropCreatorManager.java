package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.resources.TreesResourceManager;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class GlobalDropCreatorManager extends JsonReloadListener<DropCreator> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceLocation, ConfiguredDropCreator> entries = Maps.newHashMap();

    public GlobalDropCreatorManager() {
        super("drop_creators/global", DropCreator.class, "");
    }

    @Override
    public void registerAppliers() {
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> preparedObject, TreesResourceManager resourceManager, ApplicationType applicationType) {
        preparedObject.forEach((registryName, element) -> {
            if (!element.isJsonObject()) {
                LOGGER.warn("Skipping loading Global Drop Creator \"{}\" as its root element is not a Json object.", registryName);
                return;
            }

            JsonDeserialisers.CONFIGURED_DROP_CREATOR.deserialise(element.getAsJsonObject())
                    .ifSuccessOrElse(
                            result -> entries.put(registryName, result),
                            error -> LOGGER.error("Error loading Global Drop Creator \"{}\": {}", registryName, error),
                            warning -> LOGGER.warn("Warning whilst loading Global Drop Creator \"{}\": {}", registryName, warning)
                    );
        });
    }

    public List<ConfiguredDropCreator> getAll() {
        return Lists.newLinkedList(this.entries.values());
    }

    public <C extends DropContext> void appendAll(final DropCreator.DropType<C> dropType, final C context) {
        this.getAll().forEach(configuration -> configuration.getConfigurable()
                .appendDrops(configuration, dropType, context));
    }

    public ConfiguredDropCreator get(final ResourceLocation registryName) {
        return this.entries.get(registryName);
    }

    public void put(final ResourceLocation registryName, final ConfiguredDropCreator configuredDropCreator) {
        this.entries.put(registryName, configuredDropCreator);
    }

    /**
     * Do nothing on load, since we don't need to set anything up on game setup here.
     *
     * @param resourceManager The {@link IResourceManager} object.
     * @return A {@link CompletableFuture} that does nothing.
     */
    @Override
    public CompletableFuture<Void> load(TreesResourceManager resourceManager) {
        return CompletableFuture.runAsync(() -> {
        });
    }

    /**
     * Do nothing on load, since we don't need to set anything up on game setup here.
     *
     * @param resourceManager The {@link IResourceManager} object.
     * @return A {@link CompletableFuture} that does nothing.
     */
    @Override
    public CompletableFuture<Void> setup(TreesResourceManager resourceManager) {
        return CompletableFuture.runAsync(() -> {
        });
    }

}
