package com.ferreusveritas.dynamictrees.api.resource.loading;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public interface ResourceLoader<R> {

    CompletableFuture<Void> load(ResourceManager resourceManager);

    CompletableFuture<Void> gatherData(ResourceManager resourceManager);

    CompletableFuture<Void> setup(ResourceManager resourceManager);

    CompletableFuture<ResourceAccessor<R>> prepareReload(ResourceManager resourceManager);

    void reload(CompletableFuture<ResourceAccessor<R>> future, ResourceManager resourceManager);

    void applyOnLoad(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager);

    void applyOnGatherData(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager);

    void applyOnSetup(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager);

    void applyOnReload(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager);

}
