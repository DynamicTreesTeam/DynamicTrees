package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Container for a resource object that is keyed by its location.
 *
 * @param <R> the type of the resource object
 * @author Harley O'Connor
 */
public final class Resource<R> {

    private final ResourceLocation location;
    private final R resource;

    public Resource(ResourceLocation location, R resource) {
        this.location = location;
        this.resource = resource;
    }

    public <N> Resource<N> map(Function<R, N> mapper) {
        return new Resource<>(this.location, mapper.apply(this.resource));
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public R getResource() {
        return resource;
    }

}
