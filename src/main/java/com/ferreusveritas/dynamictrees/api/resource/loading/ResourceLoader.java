package com.ferreusveritas.dynamictrees.api.resource.loading;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import net.minecraft.resources.IResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Harley O'Connor
 */
public interface ResourceLoader<R> {

    CompletableFuture<Void> load(IResourceManager resourceManager);

    CompletableFuture<Void> gatherData(IResourceManager resourceManager);

    CompletableFuture<Void> setup(IResourceManager resourceManager);

    CompletableFuture<ResourceAccessor<R>> prepareReload(IResourceManager resourceManager);

    void reload(CompletableFuture<ResourceAccessor<R>> future, IResourceManager resourceManager);

    void applyOnLoad(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager);

    void applyOnGatherData(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager);

    void applyOnSetup(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager);

    void applyOnReload(ResourceAccessor<R> resourceAccessor, IResourceManager resourceManager);

}
