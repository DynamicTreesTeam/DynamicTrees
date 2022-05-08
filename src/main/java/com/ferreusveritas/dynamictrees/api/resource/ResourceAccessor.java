package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public interface ResourceAccessor<R> {

    DTResource<R> getResource(ResourceLocation key);

    Iterable<DTResource<R>> getAllResources();

    Iterable<DTResource<R>> getAllResources(Predicate<ResourceLocation> resourceFilter);

    void forEach(Consumer<DTResource<R>> resourceConsumer);

    ResourceAccessor<R> filtered(Predicate<ResourceLocation> resourceFilter);

    <N> ResourceAccessor<N> map(Function<R, N> resourceMapper);

}
