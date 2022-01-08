package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
final class SimpleResourceCollector<R> implements ResourceCollector<R> {

    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;
    public Map<ResourceLocation, Resource<R>> resources;

    @SuppressWarnings("unchecked")
    public SimpleResourceCollector(@SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.resources = (Map<ResourceLocation, Resource<R>>) mapSupplier.get();
    }

    @Override
    public Resource<R> put(Resource<R> resource) {
        return this.resources.put(resource.getLocation(), resource);
    }

    @Override
    public Resource<R> computeIfAbsent(ResourceLocation key, Supplier<Resource<R>> resourceSupplier) {
        return this.resources.computeIfAbsent(key, k -> resourceSupplier.get());
    }

    @Override
    public ResourceAccessor<R> createAccessor() {
        return new SimpleResourceAccessor<>(this.resources, this::newMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        this.resources = (Map<ResourceLocation, Resource<R>>) mapSupplier.get();
    }

    @SuppressWarnings("unchecked")
    private <N> Map<ResourceLocation, Resource<N>> newMap() {
        return (Map<ResourceLocation, Resource<N>>) this.mapSupplier.get();
    }

}
