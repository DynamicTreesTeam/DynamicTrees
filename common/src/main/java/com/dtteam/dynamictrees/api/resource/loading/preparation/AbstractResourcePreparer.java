package com.dtteam.dynamictrees.api.resource.loading.preparation;

import com.dtteam.dynamictrees.api.resource.ResourceAccessor;
import com.dtteam.dynamictrees.api.resource.ResourceCollector;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public abstract class AbstractResourcePreparer<R> implements ResourcePreparer<R> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final String folderName;
    private final String extension;
    private final int extensionLength;
    protected final ResourceCollector<R> resourceCollector;

    public AbstractResourcePreparer(String folderName, String extension, ResourceCollector<R> resourceCollector) {
        this.folderName = folderName;
        this.extension = extension;
        this.extensionLength = extension.length();
        this.resourceCollector = resourceCollector;
    }

    @Override
    public ResourceAccessor<R> prepare(ResourceManager resourceManager) {
        this.readAndPutResources(resourceManager, this.collectResources(resourceManager));
        ResourceAccessor<R> accessor = this.resourceCollector.createAccessor();
        this.resourceCollector.clear(); // Refresh collector for future use.
        return accessor;
    }

    protected Map<Identifier, Resource> collectResources(ResourceManager resourceManager) {
        return resourceManager.listResources(this.folderName, (fileName) -> fileName.getPath().endsWith(this.extension));
    }

    protected void readAndPutResources(ResourceManager resourceManager, Map<Identifier, Resource> resourceMap) {
        resourceMap.forEach((location, resource) -> {
            final Identifier resourceName = this.getResourceName(location);
            this.tryReadAndPutResource(resource, location, resourceName);
        });
    }

    private void tryReadAndPutResource(Resource resource, Identifier location, Identifier resourceName) {
        try {
            this.readAndPutResource(resource, resourceName);
        } catch (PreparationException | IOException e) {
            this.logError(location, e);
        }
    }

    protected abstract void readAndPutResource(Resource resource, Identifier resourceName)
            throws PreparationException, IOException;

    protected void logError(Identifier location, Exception e) {
        LOGGER.error("Could not read file \"{}\" due to exception.", location, e);
    }

    protected Identifier getResourceName(Identifier location) {
        final String resourcePath = location.getPath();
        final int pathIndex = this.folderName.length() + 1;
        final int pathEndIndex = resourcePath.length() - this.extensionLength;

        return Identifier.fromNamespaceAndPath(location.getNamespace(),
                resourcePath.substring(pathIndex, pathEndIndex));
    }

}