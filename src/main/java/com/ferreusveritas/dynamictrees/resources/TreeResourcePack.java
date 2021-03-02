package com.ferreusveritas.dynamictrees.resources;

import com.google.common.collect.Sets;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackFileNotFoundException;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public final class TreeResourcePack extends ResourcePack {

    private final Set<String> namespaces;

    public TreeResourcePack(final File treesFile) {
        super(treesFile);

        if (!treesFile.exists())
            LogManager.getLogger().warn("Trees directory " + treesFile.getName() + " did not exist.");

        this.namespaces = Sets.newHashSet(treesFile.list());
    }

    @Override
    public InputStream getResourceStream(@Nullable ResourcePackType type, ResourceLocation location) throws IOException {
        return new FileInputStream(this.file.getPath() + "/" + location.getNamespace() + "/" + location.getPath());
    }

    @Override
    protected InputStream getInputStream(String resourcePath) throws IOException {
        // We never use this method, so just throw an exception.
        throw new ResourcePackFileNotFoundException(this.file, resourcePath);
    }

    @Override
    protected boolean resourceExists(String resourcePath) {
        return false;
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(@Nullable ResourcePackType type, String namespace, String path, int maxDepth, Predicate<String> filter) {
        final List<File> subDirs = this.getSubDirectories(namespace);

        final List<ResourceLocation> resourceLocations = new ArrayList<>();

        // We only check for files in the base directory, as these are the only ones we use.
        for (final File subFile : subDirs) {
            final String fileName = subFile.getName();

            if (filter.test(fileName))
                resourceLocations.add(new ResourceLocation(namespace, fileName));
        }

        return resourceLocations;
    }

    @Nullable
    private File getNamespaceFile (final String namespace) {
        return this.namespaces.contains(namespace) ? new File(this.file.getPath() + "/" + namespace) : null;
    }

    private List<File> getSubDirectories (final String namespace) {
        final File namespaceDir = this.getNamespaceFile(namespace);

        if (namespaceDir == null)
            return Collections.emptyList();

        final List<File> subDirs = new ArrayList<>();

        for (final File subFile : namespaceDir.listFiles()) {
            // Ignore files at this level.
            if (!subFile.isDirectory())
                continue;

            subDirs.add(subFile);
        }

        return subDirs;
    }

    @Override
    public Set<String> getResourceNamespaces(@Nullable ResourcePackType type) {
        return this.namespaces;
    }

    @Override
    public void close() {

    }

}
