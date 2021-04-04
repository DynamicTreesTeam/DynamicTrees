package com.ferreusveritas.dynamictrees.resources;

import net.minecraft.resources.IFutureReloadListener;
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
                .thenAccept(preparedObject -> this.apply(preparedObject, resourceManager, true));
    }

    /**
     * Reloads the relevant data from the resource manager given. Called from
     * {@link DTResourceRegistries.ReloadTreesResources} on datapack reload.
     *
     * @param resourceManager The {@link IResourceManager} object.
     */
    public CompletableFuture<Void> reload (final IFutureReloadListener.IStage stage, final IResourceManager resourceManager, final Executor backgroundExecutor, final Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), backgroundExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync(preparedObject -> this.apply(preparedObject, resourceManager, false), gameExecutor);
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
     * @param firstLoad True if it's being called on first load (during game setup).
     */
    protected abstract void apply(final T preparedObject, final IResourceManager resourceManager, final boolean firstLoad);

}
