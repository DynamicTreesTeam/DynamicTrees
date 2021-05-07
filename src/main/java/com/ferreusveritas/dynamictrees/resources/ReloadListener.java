package com.ferreusveritas.dynamictrees.resources;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * A simplified version of {@link net.minecraft.client.resources.ReloadListener} that omits
 * things we don't use (namely profilers), and adds the load capability for initial loading.
 *
 * @param <T> The type of {@link Object} returned by {@link #prepare(IResourceManager)}.
 * @author Harley O'Connor
 */
public abstract class ReloadListener<T> {

    public enum ApplicationType {
        LOAD,
        SETUP,
        RELOAD
    }

    protected final String folderName;

    public ReloadListener(String folderName) {
        this.folderName = folderName;
    }

    /**
     * Loads the relevant data from the resource manager given. This should only be
     * called on initial game load, allowing apply to handle registering to Forge
     * registries.
     *
     * @param resourceManager The {@link IResourceManager} object.
     * @return The {@link CompletableFuture<Void>} that loads the relevant data.
     */
    public CompletableFuture<Void> load (final IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), Util.backgroundExecutor())
                .thenAccept(preparedObject -> this.apply(preparedObject, resourceManager, ApplicationType.LOAD));
    }

    public CompletableFuture<Void> setup(final IResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), Util.backgroundExecutor())
                .thenAccept(preparedObject -> this.apply(preparedObject, resourceManager, ApplicationType.SETUP));
    }

    /**
     * Prepares reload by creating a {@link CompletableFuture} that calls
     * {@link #prepare(IResourceManager)}.
     *
     * @param resourceManager The {@link IResourceManager} object.
     * @param backgroundExecutor The {@link Executor} to prepare files on.
     */
    public CompletableFuture<T> prepareReload(final IResourceManager resourceManager, final Executor backgroundExecutor) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), backgroundExecutor);
    }

    /**
     * Reloads the relevant data from the prepared {@link CompletableFuture} supplied by
     * {@link #prepareReload(IResourceManager, Executor)}.
     *
     * @param future The {@link CompletableFuture} created by {@link #prepareReload(IResourceManager, Executor)}.
     * @param resourceManager The {@link IResourceManager} object.
     */
    public void reload(CompletableFuture<T> future, final IResourceManager resourceManager) {
        this.apply(future.join(), resourceManager, ApplicationType.RELOAD);
    }

    /**
     * Prepares data for being read into the format of <tt>T</tt>.
     *
     * @param resourceManagerIn The {@link IResourceManager} object.
     */
    protected abstract T prepare(final IResourceManager resourceManagerIn);

    /**
     * Applies the given data.
     *
     * @param preparedObject The prepared/formatted data.
     * @param resourceManager The {@link IResourceManager} object.
     * @param applicationType The {@link ApplicationType} to use.
     */
    protected abstract void apply(final T preparedObject, final IResourceManager resourceManager, final ApplicationType applicationType);

}
