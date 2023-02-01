package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
final class SimpleResourceCollector<R> implements ResourceCollector<R> {

    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;
    public Map<ResourceLocation, DTResource<R>> resources;

    @SuppressWarnings("unchecked")
    public SimpleResourceCollector(@SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.resources = (Map<ResourceLocation, DTResource<R>>) mapSupplier.get();
    }

    @Override
    public DTResource<R> put(DTResource<R> resource) {
        return this.resources.put(resource.getLocation(), resource);
    }

    @Override
    public DTResource<R> computeIfAbsent(ResourceLocation key, Supplier<DTResource<R>> resourceSupplier) {
        return this.resources.computeIfAbsent(key, k -> resourceSupplier.get());
    }

    @Override
    public ResourceAccessor<R> createAccessor() {
        return new SimpleResourceAccessor<>(this.resources, this::newMap);
    }

    @Override
    public void clear() {
        this.resources = newMap();
    }

    @SuppressWarnings("unchecked")
    private <N> Map<ResourceLocation, DTResource<N>> newMap() {
        return (Map<ResourceLocation, DTResource<N>>) this.mapSupplier.get();
    }

}
