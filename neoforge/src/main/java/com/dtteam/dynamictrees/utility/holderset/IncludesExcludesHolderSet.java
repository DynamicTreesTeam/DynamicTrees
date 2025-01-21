package com.dtteam.dynamictrees.utility.holderset;

import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.holdersets.CompositeHolderSet;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IncludesExcludesHolderSet<T> extends CompositeHolderSet<T> {

    public static <T> MapCodec<? extends ICustomHolderSet<T>> mapCodec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
        Codec<HolderSet<T>> holderSetCodec = HolderSetCodec.create(registryKey, holderCodec, forceList);
        return RecordCodecBuilder.<IncludesExcludesHolderSet<T>>mapCodec(builder -> builder.group(
                holderSetCodec.fieldOf("includes").forGetter(IncludesExcludesHolderSet::includes),
                holderSetCodec.fieldOf("excludes").forGetter(IncludesExcludesHolderSet::excludes)
        ).apply(builder, IncludesExcludesHolderSet::new));
    }

    public static  <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> streamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
        return StreamCodec.composite(
                ByteBufCodecs.holderSet(resourceKey), IncludesExcludesHolderSet::doesInclude,
                ByteBufCodecs.holderSet(resourceKey), IncludesExcludesHolderSet::doesExclude,
                IncludesExcludesHolderSet::new);
    }

    private static <T> HolderSet<T> doesInclude(ICustomHolderSet<T> holders) {
        return ((IncludesExcludesHolderSet<T>)holders).includes;
    }
    private static <T> HolderSet<T> doesExclude(ICustomHolderSet<T> holders) {
        return ((IncludesExcludesHolderSet<T>)holders).excludes;
    }

    public static class Type implements HolderSetType {
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<Holder<T>> codec, boolean b) {
            return mapCodec(resourceKey, codec, b);
        }

        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> resourceKey) {
            return streamCodec(resourceKey);
        }
    }

    private final HolderSet<T> includes;
    private final HolderSet<T> excludes;

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

    public void clear() {
        this.getIncludeComponents().clear();
        this.getExcludeComponents().clear();
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
        return NeoForgeRegistryLoader.INCLUDES_EXCLUDES_HOLDER_SET_TYPE.get();
    }
}