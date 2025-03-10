package com.dtteam.dynamictrees.worldgen.holderset;

import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DTBiomeHolderSet implements IDTBiomeHolderSet {

    @Override
    public boolean containsKey(ResourceKey<Biome> biomeKey) {
        return false;
    }

    @Override
    public Stream<Holder<Biome>> stream() {
        return Stream.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Either<TagKey<Biome>, List<Holder<Biome>>> unwrap() {
        return null;
    }

    @Override
    public Optional<Holder<Biome>> getRandomElement(RandomSource random) {
        return Optional.empty();
    }

    @Override
    public Holder<Biome> get(int index) {
        return null;
    }

    @Override
    public boolean contains(Holder<Biome> holder) {
        return false;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Biome> owner) {
        return false;
    }

    @Override
    public Optional<TagKey<Biome>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public List<HolderSet<Biome>> getIncludeComponents() {
        return List.of();
    }

    @Override
    public List<HolderSet<Biome>> getExcludeComponents() {
        return List.of();
    }

    @Override
    public void addHolderSet(List<HolderSet<Biome>> components, HolderSet<Biome> holderSetSupplier) {

    }

    @Override
    public void addDelayedHolderSet(List<HolderSet<Biome>> components, Supplier<HolderSet<Biome>> holderSetSupplier) {

    }

    @Override
    public void addNameRegexMatch(List<HolderSet<Biome>> components, Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex) {

    }

    @Override
    public void addTagsRegexMatch(List<HolderSet<Biome>> components, Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex) {

    }

    @Override
    public void addOr(List<HolderSet<Biome>> components, List<HolderSet<Biome>> values) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public Iterator<Holder<Biome>> iterator() {
        return null;
    }
}