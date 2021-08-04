package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.util.CommonCollectors;
import com.google.common.collect.Lists;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public class TreesResourceManager implements IResourceManager {

    private final List<TreeResourcePack> resourcePacks = Lists.newArrayList();
    private final List<ReloadListener<?>> reloadListeners = Lists.newArrayList();

    public void addReloadListeners(final ReloadListener<?>... reloadListener) {
        this.reloadListeners.addAll(Arrays.asList(reloadListener));
    }

    public void addReloadListener(final int position, final ReloadListener<?> reloadListener) {
        this.reloadListeners.add(position, reloadListener);
    }

    /**
     * Gets the {@link #reloadListeners} for this {@link TreesResourceManager} object.
     *
     * @return The {@link #reloadListeners} for this {@link TreesResourceManager} object.
     */
    public List<ReloadListener<?>> getReloadListeners() {
        return this.reloadListeners;
    }

    public void registerJsonAppliers() {
        this.reloadListeners.stream()
                .filter(JsonApplierReloadListener.class::isInstance)
                .map(JsonApplierReloadListener.class::cast)
                .forEach(JsonApplierReloadListener::registerAppliers);
    }

    public void load() {
        this.reloadListeners.forEach(reloadListener -> reloadListener.load(this).join());
    }

    public void setup() {
        this.reloadListeners.forEach(reloadListener -> reloadListener.setup(this).join());
    }

    public CompletableFuture<?>[] prepareReload(final Executor backgroundExecutor, final Executor gameExecutor) {
        return this.reloadListeners.stream().map(reloadListener -> reloadListener.prepareReload(this, backgroundExecutor)).toArray(CompletableFuture<?>[]::new);
    }

    /**
     * Reloads the given {@link CompletableFuture}s. These <b>must</b> be given in the same order as returned from
     * {@link #prepareReload(Executor, Executor)}.
     *
     * @param futures The {@link CompletableFuture} returned from {@link #prepareReload(Executor, Executor)}.
     */
    public void reload(final CompletableFuture<?>[] futures) {
        for (int i = 0; i < futures.length; i++) {
            this.reload(this.reloadListeners.get(i), futures[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void reload(final ReloadListener<T> reloadListener, final CompletableFuture<?> future) {
        reloadListener.reload((CompletableFuture<T>) future, this);
    }

    public void addResourcePack(final TreeResourcePack treeResourcePack) {
        this.resourcePacks.add(treeResourcePack);
    }

    @Override
    public Set<String> getNamespaces() {
        return this.resourcePacks.stream()
                .map(treeResourcePack -> treeResourcePack.getNamespaces(null))
                .flatMap(Collection::stream)
                .collect(CommonCollectors.toLinkedSet());
    }

    @Override
    public IResource getResource(final ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = this.getResources(resourceLocationIn);

        if (resources.isEmpty()) {
            throw new FileNotFoundException("Could not find path '" + resourceLocationIn + "' in any tree packs.");
        }

        return resources.get(resources.size() - 1);
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        try {
            return !this.getResources(path).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private static final IResource NULL_RESOURCE = new SimpleResource(null, null, null, null);

    @Override
    public List<IResource> getResources(ResourceLocation path) throws IOException {
        return this.resourcePacks.stream().map(resourcePack -> {
                    final InputStream stream;

                    try {
                        stream = resourcePack.getResource(null, path);
                    } catch (final IOException e) {
                        return NULL_RESOURCE; // This resource pack did not have this resource.
                    }

                    return new SimpleResource(resourcePack.getName(), path, stream, null);
                }).filter(resourcePack -> resourcePack != NULL_RESOURCE) // Filter out non-existent resources.
                .collect(Collectors.toList());
    }

    protected Stream<ResourceLocation> resourceLocationStream(String path, Predicate<String> filter) {
        return this.resourcePacks.stream()
                .map(
                        resourcePack -> resourcePack.getResourceNamespaces().stream()
                                .map(namespace -> resourcePack.getResources(null, namespace, path, Integer.MAX_VALUE, filter))
                                .flatMap(Collection::stream)
                                .collect(CommonCollectors.toLinkedSet())
                ).flatMap(Collection::stream);
    }

    @Override
    public Collection<ResourceLocation> listResources(String path, Predicate<String> filter) {
        return this.resourceLocationStream(path, filter).collect(CommonCollectors.toAlternateLinkedSet());
    }

    public Collection<ResourceLocation> resourcesAsRegularSet(String path, Predicate<String> filter) {
        return this.resourceLocationStream(path, filter).collect(CommonCollectors.toLinkedSet());
    }

    @Override
    public Stream<IResourcePack> listPacks() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
    }

}
