package com.ferreusveritas.dynamictrees.api.resource.loading;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.ResourcePreparer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Util;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public abstract class AbstractResourceLoader<R> implements ResourceLoader<R> {

    private final ResourcePreparer<R> resourcePreparer;

    public AbstractResourceLoader(ResourcePreparer<R> resourcePreparer) {
        this.resourcePreparer = resourcePreparer;
    }

    @Override
    public final CompletableFuture<Void> gatherData(IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                        () -> this.resourcePreparer.prepare(resourceManager),
                        Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnGatherData(preparedObject, resourceManager)
        );
    }

    @Override
    public final CompletableFuture<Void> load(IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnLoad(preparedObject, resourceManager)
        );
    }

    @Override
    public final CompletableFuture<Void> setup(IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        ).thenAccept(preparedObject ->
                this.applyOnSetup(preparedObject, resourceManager)
        );
    }

    @Override
    public CompletableFuture<ResourceAccessor<R>> prepareReload(IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(
                () -> this.resourcePreparer.prepare(resourceManager),
                Util.backgroundExecutor()
        );
    }

    @Override
    public final void reload(CompletableFuture<ResourceAccessor<R>> future,
                                                IResourceManager resourceManager) {
        this.applyOnReload(future.join(), resourceManager);
    }

    @Override
    public void applyOnLoad(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnGatherData(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnSetup(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnReload(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager) {

    }

}
