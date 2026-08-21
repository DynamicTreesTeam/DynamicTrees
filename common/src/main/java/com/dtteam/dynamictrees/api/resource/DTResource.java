package com.dtteam.dynamictrees.api.resource;

import net.minecraft.resources.Identifier;

import java.util.function.Function;

/**
 * Container for a resource object that is keyed by its location.
 *
 * @param location the location of the resource object
 * @param resource the type of the resource object
 * @author Harley O'Connor
 */
public record DTResource<R>(Identifier location, R resource) {

    public <N> DTResource<N> map(Function<R, N> mapper) {
        return new DTResource<>(this.location, mapper.apply(this.resource));
    }

}
