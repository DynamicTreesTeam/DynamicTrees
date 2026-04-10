package com.dtteam.dynamictrees.treepack;

import com.mojang.logging.LogUtils;
import net.minecraft.FileUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Credits: A lot of the file reading code was based off {@link PathPackResources}.
 *
 * @author Harley O'Connor
 */
public class TreePackResources extends PathPackResources implements com.dtteam.dynamictrees.api.resource.TreeResourcePack {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Path root;
    public TreePackResources(PackLocationInfo location, Path root) {
        super(location, root);
        this.root = root;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, Identifier location) {
        return this.getRootResource(getPathFromLocation(location));
    }

    private static String[] getPathFromLocation(Identifier location) {
        String[] parts = location.getPath().split("/");
        String[] result = new String[parts.length + 1];
        result[0] = location.getNamespace();
        System.arraycopy(parts, 0, result, 1, parts.length);
        return result;
    }

    @Override
    public void listResources(@Nullable PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        FileUtil.decomposePath(path)
                .ifSuccess(parts -> net.minecraft.server.packs.PathPackResources.listPath(namespace, this.root.resolve(namespace).toAbsolutePath(), parts, resourceOutput))
                .ifError(dataResult -> LOGGER.error("Invalid path {}: {}", path, dataResult.message()));
    }

    @Override
    public Set<String> getNamespaces(@Nullable PackType type) {
        try {
            try (Stream<Path> walker = Files.walk(this.root, 1)) {
                return walker
                        .filter(Files::isDirectory)
                        .map(this.root::relativize)
                        .filter(p -> p.getNameCount() > 0) // Skip the root entry
                        .map(p -> p.toString().replaceAll("/$", "")) // Remove the trailing slash, if present
                        .filter(s -> !s.isEmpty()) // Filter empty strings, otherwise empty strings default to minecraft namespace in Identifiers
                        .collect(Collectors.toSet());
            }
        } catch (IOException | AssertionError e) {
            return Set.of();
        }
    }

}