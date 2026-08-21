package com.dtteam.dynamictrees.worldgen.holderset;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

import java.util.*;
import java.util.stream.Collectors;

public abstract class StreamBackedHolderSet<T> implements HolderSet<T> {
    public List<Holder<T>> contents() {
        return this.stream().collect(Collectors.toList());
    }

    // These sets are eagerly computed from their backing source (a registry lookup, a
    // supplier, etc.) rather than lazily resolved against a tag registry, so they're always
    // "bound" once constructible - mirrors vanilla HolderSet.Direct's isBound(), not
    // HolderSet.Named's (which tracks whether a deferred tag binding has resolved yet).
    @Override
    public boolean isBound() {
        return true;
    }

    public Set<Holder<T>> contentsSet() {
        return this.stream().collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return this.contents().size();
    }

    @Override
    public Spliterator<Holder<T>> spliterator() {
        return this.stream().spliterator();
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return Util.getRandomSafe(this.contents(), random);
    }

    @Override
    public Holder<T> get(int index) {
        return this.contents().get(index);
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return this.stream().anyMatch(h -> Objects.equals(h, holder));
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }
}
