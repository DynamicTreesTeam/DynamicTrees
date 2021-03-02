package com.ferreusveritas.dynamictrees.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class TreesResourceManager implements IResourceManager {

    private final List<TreeResourcePack> resourcePacks = Lists.newArrayList();
    private final Set<String> namespaces = Sets.newHashSet();

    public TreesResourceManager() {
        // Create the priority trees file. Added first (so stored at the front) so that the user's modifications take priority.
        final File mainTreeFolder = new File("trees/");

        if (!mainTreeFolder.exists())
            mainTreeFolder.mkdir();

        this.resourcePacks.add(new TreeResourcePack(mainTreeFolder));
    }

    public void addResourcePack (final TreeResourcePack treeResourcePack) {
        this.resourcePacks.add(treeResourcePack);
    }

    @Override
    public Set<String> getResourceNamespaces() {
        return this.namespaces;
    }

    @Override
    public IResource getResource(final ResourceLocation resourceLocationIn) throws IOException {
        return new SimpleResource("", resourceLocationIn, this.resourcePacks.get(0).getResourceStream(null, resourceLocationIn), null);
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        return false;
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation resourceLocationIn) throws IOException {
        final List<IResource> resources = new ArrayList<>();

        for (final TreeResourcePack resourcePack : this.resourcePacks) {
            resources.add(new SimpleResource("", resourceLocationIn, resourcePack.getResourceStream(null, resourceLocationIn), null));
        }

        return resources;
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(String pathIn, Predicate<String> filter) {
        final Set<ResourceLocation> resourceLocations = Sets.newHashSet();

        for (final TreeResourcePack resourcePack : this.resourcePacks) {
            for (final String namespace : resourcePack.getResourceNamespaces(null)) {
                resourceLocations.addAll(resourcePack.getAllResourceLocations(null, namespace, pathIn, 0, filter));
            }
        }

        return resourceLocations;
    }

    @Override
    public Stream<IResourcePack> getResourcePackStream() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
    }

}
