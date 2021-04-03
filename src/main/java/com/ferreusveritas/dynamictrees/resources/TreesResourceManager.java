package com.ferreusveritas.dynamictrees.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class TreesResourceManager implements IResourceManager {

    private final List<TreeResourcePack> resourcePacks = Lists.newArrayList();
    private final List<ReloadListener<?>> reloadListeners = Lists.newArrayList();

    public TreesResourceManager() {
        final File mainTreeFolder = new File("trees/");

        // Create the trees folder if it doesn't already exist.
        if (!mainTreeFolder.exists() && !mainTreeFolder.mkdir()) {
            LogManager.getLogger().error("Failed to create main 'trees' folder in your Minecraft directory.");
            return;
        }

        // Create a resource pack and add to the resource pack list (first so the user's modifications take priority).
        this.resourcePacks.add(new TreeResourcePack(mainTreeFolder.toPath().toAbsolutePath()));
    }

    public void addReloadListeners(final ReloadListener<?>... reloadListener) {
        this.reloadListeners.addAll(Arrays.asList(reloadListener));
    }

    public void addReloadListener(final int position, final ReloadListener<?> reloadListener) {
        this.reloadListeners.add(position, reloadListener);
    }

    public void load () {
        this.reloadListeners.forEach(reloadListener -> reloadListener.load(this));
        Collections.reverse(this.reloadListeners); // Reverse order so completable futures are executed in the correct order.
    }

    public List<CompletableFuture<?>> reload (final IFutureReloadListener.IStage stage, final Executor backgroundExecutor, final Executor gameExecutor) {
        final List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        this.reloadListeners.forEach(reloadListener -> completableFutures.add(reloadListener.reload(stage, this, backgroundExecutor, gameExecutor)));
        return completableFutures;
    }

    public void addResourcePack (final TreeResourcePack treeResourcePack) {
        this.resourcePacks.add(treeResourcePack);
    }

    @Override
    public Set<String> getResourceNamespaces() {
        final Set<String> namespaces = new HashSet<>();
        this.resourcePacks.forEach(treeResourcePack -> namespaces.addAll(treeResourcePack.getResourceNamespaces(null)));
        return namespaces;
    }

    @Override
    public IResource getResource(final ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = this.getAllResources(resourceLocationIn);

        if (resources.size() < 1)
            throw new FileNotFoundException("Could not find path '" + resourceLocationIn + "' in any tree packs.");

        return this.getAllResources(resourceLocationIn).get(0);
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        return false;
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = new ArrayList<>();

        for (final TreeResourcePack resourcePack : this.resourcePacks) {
            InputStream stream;

            try {
                stream = resourcePack.getResourceStream(null, resourceLocationIn);
            } catch (FileNotFoundException e) {
                continue;
            }

            resources.add(new SimpleResource("", resourceLocationIn, stream, null));
        }

        return resources;
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(String pathIn, Predicate<String> filter) {
        final Set<ResourceLocation> resourceLocations = Sets.newHashSet();

        for (final TreeResourcePack resourcePack : this.resourcePacks) {
            for (final String namespace : resourcePack.getResourceNamespaces(null)) {
                resourceLocations.addAll(resourcePack.getAllResourceLocations(null, namespace, pathIn, Integer.MAX_VALUE, filter));
            }
        }

        return resourceLocations;
    }

    @Override
    public Stream<IResourcePack> getResourcePackStream() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
    }

}