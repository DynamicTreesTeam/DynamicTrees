package com.dtteam.dynamictrees.api.resource;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public interface ResourceCollector<R> {

    DTResource<R> put(DTResource<R> resource);

    DTResource<R> computeIfAbsent(Identifier key, Supplier<DTResource<R>> resourceSupplier);

    ResourceAccessor<R> createAccessor();

    void clear();

    static <R> ResourceCollector<R> unordered() {
        return new SimpleResourceCollector<>(Maps::newConcurrentMap);
    }

    static <R> ResourceCollector<R> ordered() {
        return new SimpleResourceCollector<>(() -> Collections.synchronizedMap(new LinkedHashMap<>()));
    }

}
