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
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

    public void registerJsonAppliers() {
        this.reloadListeners.stream()
                .filter(JsonApplierReloadListener.class::isInstance)
                .map(JsonApplierReloadListener.class::cast)
                .forEach(JsonApplierReloadListener::registerAppliers);
    }

    public void load () {
        this.reloadListeners.forEach(reloadListener -> reloadListener.load(this).join());
    }

    public void setup () {
        this.reloadListeners.forEach(reloadListener -> reloadListener.setup(this).join());
    }

    public void reload (final IFutureReloadListener.IStage stage, final Executor backgroundExecutor, final Executor gameExecutor) {
        this.reloadListeners.forEach(reloadListener -> reloadListener.reload(stage, this, backgroundExecutor, gameExecutor).join());
    }

    public void addResourcePack (final TreeResourcePack treeResourcePack) {
        this.resourcePacks.add(treeResourcePack);
    }

    @Override
    public Set<String> getNamespaces() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack.getNamespaces(null)).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public IResource getResource(final ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = this.getResources(resourceLocationIn);

        if (resources.size() < 1)
            throw new FileNotFoundException("Could not find path '" + resourceLocationIn + "' in any tree packs.");

        return this.getResources(resourceLocationIn).get(resources.size() - 1);
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        return false;
    }

    @Override
    public List<IResource> getResources(ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = new ArrayList<>();

        // Add ModTreeResourcePacks resources first so that the user's changes in /trees take priority.
        this.addResources(resources, this.resourcePacks.stream().filter(resourcePack -> resourcePack instanceof ModTreeResourcePack)
                .collect(Collectors.toList()), resourceLocationIn);

        this.addResources(resources, this.resourcePacks.stream().filter(resourcePack -> !(resourcePack instanceof ModTreeResourcePack))
                .collect(Collectors.toList()), resourceLocationIn);

        return resources;
    }

    private void addResources(final List<IResource> resources, final List<TreeResourcePack> resourcePacks, ResourceLocation resourceLocationIn) {
        for (final TreeResourcePack resourcePack : resourcePacks) {
            InputStream stream;

            try {
                stream = resourcePack.getResource(null, resourceLocationIn);
            } catch (IOException e) {
                continue;
            }

            resources.add(new SimpleResource("", resourceLocationIn, stream, null));
        }
    }

    @Override
    public Collection<ResourceLocation> listResources(String path, Predicate<String> filter) {
        return this.resourcePacks.stream().map(resourcePack -> resourcePack.getResourceNamespaces().stream()
                .map(namespace -> resourcePack.getResources(null, namespace, path, Integer.MAX_VALUE, filter))
                .flatMap(Collection::stream).collect(Collectors.toSet())).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Stream<IResourcePack> listPacks() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
    }

}
