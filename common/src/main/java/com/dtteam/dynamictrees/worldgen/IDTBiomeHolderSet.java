package com.dtteam.dynamictrees.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IDTBiomeHolderSet extends HolderSet<Biome> {

    boolean containsKey(ResourceKey<Biome> biomeKey);

    Stream<Holder<Biome>> stream();

    List<HolderSet<Biome>> getIncludeComponents();

    List<HolderSet<Biome>> getExcludeComponents();

    void addHolderSet(List<HolderSet<Biome>> components, HolderSet<Biome> holderSetSupplier);

    void addDelayedHolderSet(List<HolderSet<Biome>> components, Supplier<HolderSet<Biome>> holderSetSupplier);

    void addNameRegexMatch (List<HolderSet<Biome>> components,  Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex);

    void addTagsRegexMatch (List<HolderSet<Biome>> components, Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex);

    void addOr(List<HolderSet<Biome>> components, List<HolderSet<Biome>> values);

    void clear();

}
