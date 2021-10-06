package com.ferreusveritas.dynamictrees.api.resource;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@link net.minecraft.resources.IResourcePack} that reads from the {@code trees} folder.
 *
 * @author Harley O'Connor
 */
public interface TreeResourcePack extends IResourcePack {

    String FOLDER = "trees";

    default InputStream getResource(ResourceLocation location) throws IOException {
        return this.getResource(null, location);
    }

    default Collection<ResourceLocation> getResources(String namespace, String path, int maxDepth,
                                                      Predicate<String> filter) {
        return this.getResources(null, namespace, path, maxDepth, filter);
    }

    default boolean hasResource(ResourceLocation location) {
        return this.hasResource(null, location);
    }

    default Set<String> getNamespaces() {
        return this.getNamespaces(null);
    }

}
