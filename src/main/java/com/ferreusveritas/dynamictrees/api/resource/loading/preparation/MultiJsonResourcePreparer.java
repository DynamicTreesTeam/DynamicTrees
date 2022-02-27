package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.ResourceCollector;
import com.ferreusveritas.dynamictrees.api.resource.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class MultiJsonResourcePreparer extends
        AbstractResourcePreparer<Iterable<JsonElement>> {

    private static final String JSON_EXTENSION = ".json";

    private static final Gson GSON = (new GsonBuilder())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public MultiJsonResourcePreparer(String folderName) {
        this(folderName, ResourceCollector.ordered());
    }

    public MultiJsonResourcePreparer(String folderName, ResourceCollector<Iterable<JsonElement>> resourceCollector) {
        super(folderName, JSON_EXTENSION, resourceCollector);
    }

    @Override
    protected void readAndPutResources(Collection<ResourceLocation> resourceLocations,
                                       IResourceManager resourceManager) {
        resourceLocations.forEach(location -> {
            final ResourceLocation resourceName = this.getResourceName(location);
            this.tryReadAndPutResource(resourceManager, location, resourceName);
        });
    }

    private void tryReadAndPutResource(IResourceManager resourceManager, ResourceLocation location,
                                       ResourceLocation resourceName) {
        try {
            this.readAndPutResource(resourceManager, location, resourceName);
        } catch (PreparationException | IOException e) {
            this.logError(location, e);
        }
    }

    @Override
    protected void readAndPutResource(IResource resource, ResourceLocation resourceName)
            throws PreparationException, IOException {

    }

    private void readAndPutResource(IResourceManager resourceManager, ResourceLocation location,
                                    ResourceLocation resourceName) throws PreparationException, IOException {
        this.computeResourceListIfAbsent(resourceName)
                .addAll(this.collectResources(resourceManager, location));
    }

    private List<JsonElement> computeResourceListIfAbsent(ResourceLocation resourceName) {
        return (List<JsonElement>)
                this.resourceCollector.computeIfAbsent(resourceName,
                                () -> new Resource<>(resourceName, new LinkedList<>())
                        ).getResource();
    }

    private List<JsonElement> collectResources(IResourceManager resourceManager, ResourceLocation location)
            throws IOException, PreparationException {
        final List<JsonElement> resources = new LinkedList<>();
        for (IResource resource : resourceManager.getResources(location)) {
            resources.add(JsonResourcePreparer.readResource(resource));
        }
        return resources;
    }

}
