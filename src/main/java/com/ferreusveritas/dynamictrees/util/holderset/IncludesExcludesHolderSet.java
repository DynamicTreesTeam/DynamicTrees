package com.ferreusveritas.dynamictrees.util.holderset;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import net.minecraftforge.registries.holdersets.CompositeHolderSet;
import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IncludesExcludesHolderSet<T> extends CompositeHolderSet<T> {
    public static <T> Codec<? extends ICustomHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
        Codec<HolderSet<T>> holderSetCodec = HolderSetCodec.create(registryKey, holderCodec, forceList);
        return RecordCodecBuilder.<IncludesExcludesHolderSet<T>>create(builder -> builder.group(
                holderSetCodec.fieldOf("includes").forGetter(IncludesExcludesHolderSet::includes),
                holderSetCodec.fieldOf("excludes").forGetter(IncludesExcludesHolderSet::excludes)
        ).apply(builder, IncludesExcludesHolderSet::new));
    }

    private final HolderSet<T> includes;
    private final HolderSet<T> excludes;

    @SuppressWarnings("unchecked")
    public static <T> Class<IncludesExcludesHolderSet<T>> getCastedClass() {
        return (Class<IncludesExcludesHolderSet<T>>) (Class<?>) IncludesExcludesHolderSet.class;
    }

    public static <T> IncludesExcludesHolderSet<T> emptyAnds() {
        return new IncludesExcludesHolderSet<>(new AndHolderSet<>(new ArrayList<>()), new AndHolderSet<>(new ArrayList<>()));
    }

    public IncludesExcludesHolderSet(HolderSet<T> includes, HolderSet<T> excludes) {
        super(List.of(includes, excludes));
        this.includes = includes;
        this.excludes = excludes;
    }

    public HolderSet<T> includes() {
        return this.includes;
    }

    public List<HolderSet<T>> getIncludeComponents() {
        return this.includes instanceof CompositeHolderSet<T> compositeHolderSet ? compositeHolderSet.getComponents() : null;
    }

    public List<HolderSet<T>> getExcludeComponents() {
        return this.excludes instanceof CompositeHolderSet<T> compositeHolderSet ? compositeHolderSet.getComponents() : null;
    }

    public HolderSet<T> excludes() {
        return this.excludes;
    }

    @Override
    protected Set<Holder<T>> createSet() {
        return this.includes.stream().filter(holder -> !this.excludes.contains(holder)).collect(Collectors.toSet());
    }

    @Override
    public HolderSetType type() {
        return DTRegistries.INCLUDES_EXCLUDES_HOLDER_SET_TYPE.get();
    }
}
