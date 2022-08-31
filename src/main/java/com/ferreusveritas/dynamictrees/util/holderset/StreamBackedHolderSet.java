package com.ferreusveritas.dynamictrees.util.holderset;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

public abstract class StreamBackedHolderSet<T> implements HolderSet<T> {
    public List<Holder<T>> contents() {
        return this.stream().collect(Collectors.toList());
    }

    public Set<Holder<T>> contentsSet() {
        return this.stream().collect(Collectors.toSet());
    }

    public int size() {
        return this.contents().size();
    }

    public Spliterator<Holder<T>> spliterator() {
        return this.stream().spliterator();
    }

    public Iterator<Holder<T>> iterator() {
        return this.stream().iterator();
    }

    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return Util.getRandomSafe(this.contents(), random);
    }

    public Holder<T> get(int index) {
        return this.contents().get(index);
    }

    public boolean isValidInRegistry(Registry<T> registry) {
        return true;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return this.stream().anyMatch(h -> Objects.equals(h, holder));
    }
}
