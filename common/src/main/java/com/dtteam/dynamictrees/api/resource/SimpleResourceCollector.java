package com.dtteam.dynamictrees.api.resource;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
final class SimpleResourceCollector<R> implements ResourceCollector<R> {

    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;
    public Map<Identifier, DTResource<R>> resources;

    @SuppressWarnings("unchecked")
    public SimpleResourceCollector(@SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.resources = (Map<Identifier, DTResource<R>>) mapSupplier.get();
    }

    public DTResource<R> put(DTResource<R> resource) {
        return this.resources.put(resource.location(), resource);
    }

    public DTResource<R> computeIfAbsent(Identifier key, Supplier<DTResource<R>> resourceSupplier) {
        return this.resources.computeIfAbsent(key, k -> resourceSupplier.get());
    }

    public ResourceAccessor<R> createAccessor() {
        return new SimpleResourceAccessor<>(this.resources, this::newMap);
    }

    public void clear() {
        this.resources = newMap();
    }

    @SuppressWarnings("unchecked")
    private <N> Map<Identifier, DTResource<N>> newMap() {
        return (Map<Identifier, DTResource<N>>) this.mapSupplier.get();
    }

}
