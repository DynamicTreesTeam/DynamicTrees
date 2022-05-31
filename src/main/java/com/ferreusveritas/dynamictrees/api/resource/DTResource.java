package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Container for a resource object that is keyed by its location.
 *
 * @param <R> the type of the resource object
 * @author Harley O'Connor
 */
public final class DTResource<R> {

    private final ResourceLocation location;
    private final R resource;

    public DTResource(ResourceLocation location, R resource) {
        this.location = location;
        this.resource = resource;
    }

    public <N> DTResource<N> map(Function<R, N> mapper) {
        return new DTResource<>(this.location, mapper.apply(this.resource));
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public R getResource() {
        return resource;
    }

}
