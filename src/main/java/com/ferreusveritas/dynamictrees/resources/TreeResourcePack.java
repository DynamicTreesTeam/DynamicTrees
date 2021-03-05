package com.ferreusveritas.dynamictrees.resources;

import com.google.common.base.Joiner;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
public final class TreeResourcePack extends ResourcePack {

    private final Path path;

    public TreeResourcePack(final File treesFile) {
        super(treesFile);

        if (!treesFile.exists())
            LogManager.getLogger().warn("Trees directory " + treesFile.getName() + " did not exist.");

        this.path = treesFile.toPath();
    }

    @Nullable
    @Override
    public InputStream getResourceStream(@Nullable ResourcePackType type, ResourceLocation location) throws IOException {
        final Path path = new File(this.path + "/" + location.getNamespace() + "/" + location.getPath()).toPath().toAbsolutePath();
        if(!Files.exists(path))
            return null;
        return Files.newInputStream(path, StandardOpenOption.READ);
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
    public Collection<ResourceLocation> getAllResourceLocations(@Nullable ResourcePackType type, String namespace, String pathIn, int maxDepth, Predicate<String> filter) {
        try {
            Path root = new File(this.path + "/" + namespace).toPath().toAbsolutePath();
            Path inputPath = root.getFileSystem().getPath(pathIn);

            return Files.walk(root).
                    map(path -> root.relativize(path.toAbsolutePath())).
                    filter(path -> path.getNameCount() <= maxDepth). // Make sure the depth is within bounds
                    filter(path -> !path.toString().endsWith(".mcmeta")). // Ignore .mcmeta files
                    filter(path -> path.startsWith(inputPath)). // Make sure the target path is inside this one
                    filter(path -> filter.test(path.getFileName().toString())). // Test the file name against the predicate
                    // Finally we need to form the RL, so use the first name as the domain, and the rest as the path
                    // It is VERY IMPORTANT that we do not rely on Path.toString as this is inconsistent between operating systems
                    // Join the path names ourselves to force forward slashes
                    map(path -> new ResourceLocation(namespace, Joiner.on('/').join(path))).
                    collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

//        final List<File> subDirs = this.getSubDirectories(namespace);
//
//        final List<ResourceLocation> resourceLocations = new ArrayList<>();
//
//        // We only check for files in the base directory, as these are the only ones we use.
//        for (final File subFile : subDirs) {
//            final String fileName = subFile.getName();
//
//            if (filter.test(fileName))
//                resourceLocations.add(new ResourceLocation(namespace, fileName));
//        }
//
//        return resourceLocations;
    }

//    private File getNamespaceFile (final String namespace) {
//        return new File(this.path + "/" + namespace);
//    }
//
//    private List<File> getSubDirectories (final String namespace) {
//        final File namespaceDir = this.getNamespaceFile(namespace);
//
//        if (namespaceDir == null)
//            return Collections.emptyList();
//
//        final List<File> subDirs = new ArrayList<>();
//
//        for (final File subFile : namespaceDir.listFiles()) {
//            // Ignore files at this level.
//            if (!subFile.isDirectory())
//                continue;
//
//            subDirs.add(subFile);
//        }
//
//        return subDirs;
//    }

    @Override
    public Set<String> getResourceNamespaces(@Nullable final ResourcePackType type) {
        try {
            Path root = this.path.toAbsolutePath();
            return Files.walk(root,1)
                    .map(path -> root.relativize(path.toAbsolutePath()))
                    .filter(path -> path.getNameCount() > 0) // skip the root entry
                    .map(p->p.toString().replaceAll("/$","")) // remove the trailing slash, if present
                    .filter(s -> !s.isEmpty()) //filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    @Override
    public void close() { }

}
