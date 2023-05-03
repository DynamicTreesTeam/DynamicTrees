package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@linkplain PackResources resource pack} that reads from the {@code trees} folder.
 *
 * @author Harley O'Connor
 */
public interface TreeResourcePack extends PackResources {

    String FOLDER = "trees";

    @SuppressWarnings("ConstantConditions")
    default InputStream getResource(ResourceLocation location) throws IOException {
        return this.getResource(null, location);
    }

    @SuppressWarnings("ConstantConditions")
    default Collection<ResourceLocation> getResources(String namespace, String path,
                                                      Predicate<ResourceLocation> filter) {
        return this.getResources(null, namespace, path, filter);
    }

    @SuppressWarnings("ConstantConditions")
    default boolean hasResource(ResourceLocation location) {
        return this.hasResource(null, location);
    }

    @SuppressWarnings("ConstantConditions")
    default Set<String> getNamespaces() {
        return this.getNamespaces(null);
    }

}