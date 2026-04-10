package com.dtteam.dynamictrees.api.resource;

import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public interface ResourceAccessor<R> {

    DTResource<R> getResource(Identifier key);

    Iterable<DTResource<R>> getAllResources();

    Iterable<DTResource<R>> getAllResources(Predicate<Identifier> resourceFilter);

    void forEach(Consumer<DTResource<R>> resourceConsumer);

    ResourceAccessor<R> filtered(Predicate<Identifier> resourceFilter);

    <N> ResourceAccessor<N> map(Function<R, N> resourceMapper);

}
