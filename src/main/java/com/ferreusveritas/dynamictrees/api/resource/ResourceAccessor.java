package com.ferreusveritas.dynamictrees.api.resource;

import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Harley O'Connor
 */
public interface ResourceAccessor<R> {

    Resource<R> getResource(ResourceLocation key);

    Iterable<Resource<R>> getAllResources();

    Iterable<Resource<R>> getAllResources(Predicate<ResourceLocation> resourceFilter);

    void forEach(Consumer<Resource<R>> resourceConsumer);

    ResourceAccessor<R> filtered(Predicate<ResourceLocation> resourceFilter);

    <N> ResourceAccessor<N> map(Function<R, N> resourceMapper);

}
