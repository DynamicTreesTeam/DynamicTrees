package com.dtteam.dynamictrees.api.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.InputStream;
import java.util.Set;

/**
 * A {@linkplain PackResources resource pack} that reads from the {@code trees} folder.
 *
 * @author Harley O'Connor
 */
public interface TreeResourcePack extends PackResources {
    @SuppressWarnings("ConstantConditions")
    default IoSupplier<InputStream> getResource(Identifier location) {
        return this.getResource(null, location);
    }

    @SuppressWarnings("ConstantConditions")
    default void listResources(String namespace, String path, ResourceOutput resourceOutput) {
        this.listResources(null, namespace, path, resourceOutput);
    }

    @SuppressWarnings("ConstantConditions")
    default boolean hasResource(Identifier location) {
        return this.getResource(null, location) != null;
    }

    @SuppressWarnings("ConstantConditions")
    default Set<String> getNamespaces() {
        return this.getNamespaces(null);
    }
}