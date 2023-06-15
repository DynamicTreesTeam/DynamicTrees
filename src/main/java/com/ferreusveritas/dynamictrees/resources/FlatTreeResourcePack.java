package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack;
import com.mojang.logging.LogUtils;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
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
public class FlatTreeResourcePack extends PathPackResources implements TreeResourcePack {
    private static final Logger LOGGER = LogUtils.getLogger();

    public FlatTreeResourcePack(String packId, boolean isBuiltin, Path source) {
        super(packId, isBuiltin, source);
    }

    @Override
    public IoSupplier<InputStream> getResource(@Nullable PackType packType, ResourceLocation location) {
        return this.getRootResource(getPathFromLocation(location));
    }

    private static String[] getPathFromLocation(ResourceLocation location) {
        String[] parts = location.getPath().split("/");
        String[] result = new String[parts.length + 2];
        result[0] = FOLDER;
        result[1] = location.getNamespace();
        System.arraycopy(parts, 0, result, 2, parts.length);
        return result;
    }

    @Override
    public void listResources(@Nullable PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        FileUtil.decomposePath(path).get()
                .ifLeft(parts -> net.minecraft.server.packs.PathPackResources.listPath(namespace, resolve(FOLDER, namespace).toAbsolutePath(), parts, resourceOutput))
                .ifRight(dataResult -> LOGGER.error("Invalid path {}: {}", path, dataResult.message()));
    }

    @Override
    public Set<String> getNamespaces(@Nullable PackType type) {
        try {
            Path root = this.resolve();
            try (Stream<Path> walker = Files.walk(root, 1)) {
                return walker
                        .filter(Files::isDirectory)
                        .map(root::relativize)
                        .filter(p -> p.getNameCount() > 0) // Skip the root entry
                        .map(p -> p.toString().replaceAll("/$", "")) // Remove the trailing slash, if present
                        .filter(s -> !s.isEmpty()) // Filter empty strings, otherwise empty strings default to minecraft namespace in ResourceLocations
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            return Set.of();
        }
    }
}