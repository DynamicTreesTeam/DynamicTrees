package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;

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

    private final Map<ResourceLocation, DTResource<R>> resources;
    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;

    public SimpleResourceAccessor(Map<ResourceLocation, DTResource<R>> resources,
                                  @SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.resources = resources;
        this.mapSupplier = mapSupplier;
    }

    @Override
    public DTResource<R> getResource(ResourceLocation key) {
        return this.resources.get(key);
    }

    @Override
    public Iterable<DTResource<R>> getAllResources() {
        return new ArrayList<>(this.resources.values());
    }

    @Override
    public Iterable<DTResource<R>> getAllResources(Predicate<ResourceLocation> resourceFilter) {
        return this.resources.entrySet().stream()
                .filter(resource -> resourceFilter.test(resource.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void forEach(Consumer<DTResource<R>> resourceConsumer) {
        this.getAllResources().forEach(resourceConsumer);
    }

    @Override
    public ResourceAccessor<R> filtered(Predicate<ResourceLocation> resourceFilter) {
        final Map<ResourceLocation, DTResource<R>> resources = this.cloneResources();
        this.filterMap(resourceFilter, resources);
        return new SimpleResourceAccessor<>(resources, this.mapSupplier);
    }

    @Override
    public <N> ResourceAccessor<N> map(Function<R, N> resourceMapper) {
        final Map<ResourceLocation, DTResource<N>> mappedResources = this.freshMap();
        this.resources.forEach((location, resource) -> {
            mappedResources.put(location, new DTResource<>(location, resourceMapper.apply(resource.getResource())));
        });
        return new SimpleResourceAccessor<>(mappedResources, this.mapSupplier);
    }

    private Map<ResourceLocation, DTResource<R>> cloneResources() {
        final Map<ResourceLocation, DTResource<R>> resources = this.freshMap();
        resources.putAll(this.resources);
        return resources;
    }

    @SuppressWarnings("unchecked")
    private <N> Map<ResourceLocation, DTResource<N>> freshMap() {
        return (Map<ResourceLocation, DTResource<N>>) this.mapSupplier.get();
    }

    private void filterMap(Predicate<ResourceLocation> resourceFilter, Map<ResourceLocation, DTResource<R>> newMap) {
        for (ResourceLocation location : resources.keySet()) {
            if (!resourceFilter.test(location)) {
                newMap.remove(location);
            }
        }
    }

}
