package com.dtteam.dynamictrees.api.resource;

import net.minecraft.resources.Identifier;

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

    private final Map<Identifier, DTResource<R>> resources;
    @SuppressWarnings("rawtypes")
    private final Supplier<Map> mapSupplier;

    public SimpleResourceAccessor(Map<Identifier, DTResource<R>> resources,
                                  @SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
        this.resources = resources;
        this.mapSupplier = mapSupplier;
    }

    public DTResource<R> getResource(Identifier key) {
        return this.resources.get(key);
    }

    public Iterable<DTResource<R>> getAllResources() {
        return new ArrayList<>(this.resources.values());
    }

    public Iterable<DTResource<R>> getAllResources(Predicate<Identifier> resourceFilter) {
        return this.resources.entrySet().stream()
                .filter(resource -> resourceFilter.test(resource.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void forEach(Consumer<DTResource<R>> resourceConsumer) {
        this.getAllResources().forEach(resourceConsumer);
    }

    public ResourceAccessor<R> filtered(Predicate<Identifier> resourceFilter) {
        final Map<Identifier, DTResource<R>> resources = this.cloneResources();
        this.filterMap(resourceFilter, resources);
        return new SimpleResourceAccessor<>(resources, this.mapSupplier);
    }

    public <N> ResourceAccessor<N> map(Function<R, N> resourceMapper) {
        final Map<Identifier, DTResource<N>> mappedResources = this.freshMap();
        this.resources.forEach((location, resource) -> {
            mappedResources.put(location, new DTResource<>(location, resourceMapper.apply(resource.resource())));
        });
        return new SimpleResourceAccessor<>(mappedResources, this.mapSupplier);
    }

    private Map<Identifier, DTResource<R>> cloneResources() {
        final Map<Identifier, DTResource<R>> resources = this.freshMap();
        resources.putAll(this.resources);
        return resources;
    }

    @SuppressWarnings("unchecked")
    private <N> Map<Identifier, DTResource<N>> freshMap() {
        return (Map<Identifier, DTResource<N>>) this.mapSupplier.get();
    }

    private void filterMap(Predicate<Identifier> resourceFilter, Map<Identifier, DTResource<R>> newMap) {
        for (Identifier location : resources.keySet()) {
            if (!resourceFilter.test(location)) {
                newMap.remove(location);
            }
        }
    }

}
