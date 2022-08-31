package com.ferreusveritas.dynamictrees.util.holderset;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

public class DelayedTagEntriesHolderSet<T> extends HolderSet.Named<T> {
    private final Supplier<Registry<T>> registrySupplier;

    public DelayedTagEntriesHolderSet(Supplier<Registry<T>> registrySupplier, TagKey<T> key) {
        super(null, key);
        this.registrySupplier = registrySupplier;
    }

    @Override
    public boolean isValidInRegistry(Registry<T> registry) {
        return this.registrySupplier.get() == registry;
    }
}
