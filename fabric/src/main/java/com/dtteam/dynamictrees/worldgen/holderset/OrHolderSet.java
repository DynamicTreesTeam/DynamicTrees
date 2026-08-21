package com.dtteam.dynamictrees.worldgen.holderset;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;

import java.util.List;
import java.util.stream.Stream;

public class OrHolderSet<T> extends StreamBackedHolderSet<T> {

    private final List<HolderSet<T>> values;

    public OrHolderSet(List<HolderSet<T>> values) {
        this.values = values;
    }

    @Override
    public Stream<Holder<T>> stream() {
        return this.values.stream().flatMap(HolderSet::stream).distinct();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.values.stream().allMatch(set -> set.canSerializeIn(owner));
    }

    @Override
    public boolean isBound() {
        return this.values.stream().allMatch(HolderSet::isBound);
    }
}
