/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.ferreusveritas.dynamictrees.util.holderset;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record DelayedAnyHolderSet<T>(Supplier<Registry<T>> registrySupplier) implements HolderSet<T> {
    public Registry<T> registry() {
        return Objects.requireNonNull(this.registrySupplier.get());
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return this.registry().holders().map(Function.identity());
    }

    @Override
    public int size() {
        return this.registry().size();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.stream().toList());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return this.registry().getRandom(random);
    }

    @Override
    public Holder<T> get(int i) {
        return this.registry().getHolder(i).orElseThrow(() -> new NoSuchElementException("No element " + i + " in registry " + this.registry().key()));
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return holder.unwrapKey().map(this.registry()::containsKey).orElseGet(() -> this.registry().getResourceKey(holder.value()).isPresent());
    }

    @Override
    public boolean isValidInRegistry(Registry<T> registry) {
        return this.registry() == registry;
    }

    @Override
    public String toString() {
        return "AnySet(" + this.registry().key() + ")";
    }
}