package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
final class SimpleResourceAccessor<R> implements ResourceAccessor<R> {

    private final Map<ResourceLocation, Resource<R>> resources;
    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;

    public SimpleResourceAccessor(Map<ResourceLocation, Resource<R>> resources,
                                  @SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.resources = resources;
        this.mapSupplier = mapSupplier;
    }

    @Override
    public Resource<R> getResource(ResourceLocation key) {
        return this.resources.get(key);
    }

    @Override
    public Iterable<Resource<R>> getAllResources() {
        return new ArrayList<>(this.resources.values());
    }

    @Override
    public Iterable<Resource<R>> getAllResources(Predicate<ResourceLocation> resourceFilter) {
        return this.resources.entrySet().stream()
                .filter(resource -> resourceFilter.test(resource.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void forEach(Consumer<Resource<R>> resourceConsumer) {
        this.getAllResources().forEach(resourceConsumer);
    }

    @Override
    public ResourceAccessor<R> filtered(Predicate<ResourceLocation> resourceFilter) {
        final Map<ResourceLocation, Resource<R>> resources = this.cloneResources();
        this.filterMap(resourceFilter, resources);
        return new SimpleResourceAccessor<>(resources, this.mapSupplier);
    }

    @Override
    public <N> ResourceAccessor<N> map(Function<R, N> resourceMapper) {
        final Map<ResourceLocation, Resource<N>> mappedResources = this.freshMap();
        this.resources.forEach((location, resource) -> {
            mappedResources.put(location, new Resource<>(location, resourceMapper.apply(resource.getResource())));
        });
        return new SimpleResourceAccessor<>(mappedResources, this.mapSupplier);
    }

    private Map<ResourceLocation, Resource<R>> cloneResources() {
        final Map<ResourceLocation, Resource<R>> resources = this.freshMap();
        resources.putAll(this.resources);
        return resources;
    }

    @SuppressWarnings("unchecked")
    private <N> Map<ResourceLocation, Resource<N>> freshMap() {
        return (Map<ResourceLocation, Resource<N>>) this.mapSupplier.get();
    }

    private void filterMap(Predicate<ResourceLocation> resourceFilter, Map<ResourceLocation, Resource<R>> newMap) {
        for (ResourceLocation location : resources.keySet()) {
            if (!resourceFilter.test(location)) {
                newMap.remove(location);
            }
        }
    }

}
