package com.dtteam.dynamictrees.api.resource.loading;

import com.dtteam.dynamictrees.api.resource.ResourceAccessor;
import com.dtteam.dynamictrees.api.resource.loading.preparation.ResourcePreparer;
import net.minecraft.util.Util;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public abstract class AbstractResourceLoader<R> implements ResourceLoader<R> {

    private final ResourcePreparer<R> resourcePreparer;

    public AbstractResourceLoader(ResourcePreparer<R> resourcePreparer) {
        this.resourcePreparer = resourcePreparer;
    }

    public final CompletableFuture<Void> gatherData(ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                        () -> this.resourcePreparer.prepare(resourceManager),
                        Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnGatherData(preparedObject, resourceManager)
        );
    }

    public final CompletableFuture<Void> load(ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnLoad(preparedObject, resourceManager)
        );
    }

    public final CompletableFuture<Void> setup(ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnSetup(preparedObject, resourceManager)
        );
    }

    public CompletableFuture<ResourceAccessor<R>> prepareReload(ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        );
    }

    public final void reload(CompletableFuture<ResourceAccessor<R>> future,
                                                ResourceManager resourceManager) {
        this.applyOnReload(future.join(), resourceManager);
    }

    public void applyOnLoad(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

    }

    public void applyOnGatherData(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

    }

    public void applyOnSetup(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

    }

    public void applyOnReload(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

    }

}
