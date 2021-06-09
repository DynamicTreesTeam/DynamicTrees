package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.util.CommonCollectors;
import com.google.common.base.Joiner;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackFileNotFoundException;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Credits: A lot of the file reading code was based off {@link net.minecraftforge.fml.packs.ModFileResourcePack}.
 *
 * @author Harley O'Connor
 */
public class TreeResourcePack extends ResourcePack {

    protected final Path path;

    public TreeResourcePack(final Path path) {
        super(new File("dummy"));
        this.path = path;
    }

    @Override
    public InputStream getResource(@Nullable ResourcePackType type, ResourceLocation location) throws IOException {
        final Path path = this.getPath(location.getNamespace(), location.getPath());
        if (!Files.exists(path))
            throw new FileNotFoundException("Could not find tree resource for path '" + path + "'.");
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Override
    protected InputStream getResource(String resourcePath) throws IOException {
        // We never use this method, so just throw an exception.
        throw new ResourcePackFileNotFoundException(this.file, resourcePath);
    }

    @Override
    protected boolean hasResource(String resourcePath) {
        // We never use this method, so just return false.
        return false;
    }

    @Override
    public Collection<ResourceLocation> getResources(@Nullable ResourcePackType type, String namespace, String pathIn, int maxDepth, Predicate<String> filter) {
        try {
            Path root = this.getPath(namespace);
            Path inputPath = root.getFileSystem().getPath(pathIn);

            return Files.walk(root)
                    .map(path -> root.relativize(path.toAbsolutePath()))
                    .filter(path -> path.getNameCount() <= maxDepth) // Make sure the depth is within bounds
                    .filter(path -> !path.toString().endsWith(".mcmeta")) // Ignore .mcmeta files
                    .filter(path -> path.startsWith(inputPath)) // Make sure the target path is inside this one
                    .filter(path -> filter.test(path.getFileName().toString())) // Test the file name against the predicate
                    // Finally we need to form the RL, so use the first name as the domain, and the rest as the path
                    // It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
                    // Join the path names ourselves to force forward slashes
                    .map(path -> new ResourceLocation(namespace, Joiner.on('/').join(path)))
                    .collect(CommonCollectors.toAlternateLinkedSet());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Set<String> getResourceNamespaces() {
        return this.getNamespaces(null);
    }

    @Override
    public Set<String> getNamespaces(@Nullable final ResourcePackType type) {
        try {
            Path root = this.getPath();

            return Files.walk(root,1)
                    .map(path -> root.relativize(path.toAbsolutePath()))
                    .filter(path -> path.getNameCount() > 0) // skip the root entry
                    .map(p -> p.toString().replaceAll("/$","")) // remove the trailing slash, if present
                    .filter(s -> !s.isEmpty()) // filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
                    .collect(CommonCollectors.toLinkedSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    protected Path getPath(final String... paths) {
        return this.path.getFileSystem().getPath(this.path.toString(), paths);
    }

    @Override
    public void close() { }

}
