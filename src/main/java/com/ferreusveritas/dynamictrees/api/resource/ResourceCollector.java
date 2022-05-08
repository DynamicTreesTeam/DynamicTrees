package com.ferreusveritas.dynamictrees.api.resource;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public interface ResourceCollector<R> {

    DTResource<R> put(DTResource<R> resource);

    DTResource<R> computeIfAbsent(ResourceLocation key, Supplier<DTResource<R>> resourceSupplier);

    ResourceAccessor<R> createAccessor();

    void clear();

    static <R> ResourceCollector<R> unordered() {
        return new SimpleResourceCollector<>(Maps::newHashMap);
    }

    static <R> ResourceCollector<R> ordered() {
        return new SimpleResourceCollector<>(Maps::newLinkedHashMap);
    }

}
