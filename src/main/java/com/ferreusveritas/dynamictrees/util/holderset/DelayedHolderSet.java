package com.ferreusveritas.dynamictrees.util.holderset;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DelayedHolderSet<T> implements HolderSet<T> {
    private final Supplier<HolderSet<T>> holderSetSupplier;

    public DelayedHolderSet(Supplier<HolderSet<T>> holderSetSupplier)
    {
        this.holderSetSupplier = holderSetSupplier;
    }

    @Override
    public Stream<Holder<T>> stream() {
        return this.holderSetSupplier.get().stream();
    }

    @Override
    public int size() {
        return this.holderSetSupplier.get().size();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return this.holderSetSupplier.get().unwrap();
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return this.holderSetSupplier.get().getRandomElement(random);
    }

    @Override
    public Holder<T> get(int index) {
        return this.holderSetSupplier.get().get(index);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return this.holderSetSupplier.get().contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.holderSetSupplier.get().canSerializeIn(owner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return this.holderSetSupplier.get().unwrapKey();
    }

    @NotNull
    @Override
    public Iterator<Holder<T>> iterator() {
        return this.holderSetSupplier.get().iterator();
    }

    @Override
    public void forEach(Consumer<? super Holder<T>> action) {
        this.holderSetSupplier.get().forEach(action);
    }

    @Override
    public Spliterator<Holder<T>> spliterator() {
        return this.holderSetSupplier.get().spliterator();
    }

    @Override
    public void addInvalidationListener(Runnable runnable) {
        this.holderSetSupplier.get().addInvalidationListener(runnable);
    }

    @Override
    public SerializationType serializationType() {
        return this.holderSetSupplier.get().serializationType();
    }
}