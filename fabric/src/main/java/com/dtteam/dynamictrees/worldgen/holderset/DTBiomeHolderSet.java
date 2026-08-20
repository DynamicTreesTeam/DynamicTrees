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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DTBiomeHolderSet implements IDTBiomeHolderSet {

    private final List<HolderSet<Biome>> includeComponents = new ArrayList<>();
    private final List<HolderSet<Biome>> excludeComponents = new ArrayList<>();

    private Set<Holder<Biome>> getSet() {
        Set<Holder<Biome>> tempSet = null;
        for (HolderSet<Biome> component : this.includeComponents) {
            if (tempSet == null) {
                tempSet = new HashSet<>();
                component.forEach(tempSet::add);
            } else {
                Set<Holder<Biome>> componentSet = new HashSet<>();
                component.forEach(componentSet::add);
                tempSet.retainAll(componentSet);
            }
        }
        if (tempSet == null) {
            tempSet = new HashSet<>();
        }
        for (HolderSet<Biome> component : this.excludeComponents) {
            component.forEach(tempSet::remove);
        }
        return tempSet;
    }

    @Override
    public boolean containsKey(ResourceKey<Biome> biomeKey) {
        Set<Holder<Biome>> currentSet = this.getSet();
        for (Holder<Biome> holder : currentSet) {
            Optional<ResourceKey<Biome>> key = holder.unwrapKey();
            if (key.isPresent() && key.get().equals(biomeKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<Holder<Biome>> stream() {
        return this.getSet().stream();
    }

    @Override
    public int size() {
        return this.getSet().size();
    }

    @Override
    public Either<TagKey<Biome>, List<Holder<Biome>>> unwrap() {
        return Either.right(new ArrayList<>(this.getSet()));
    }

    @Override
    public Optional<Holder<Biome>> getRandomElement(RandomSource random) {
        Set<Holder<Biome>> set = this.getSet();
        if (set.isEmpty()) {
            return Optional.empty();
        }
        int index = random.nextInt(set.size());
        Iterator<Holder<Biome>> iterator = set.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return Optional.of(iterator.next());
    }

    @Override
    public Holder<Biome> get(int index) {
        Iterator<Holder<Biome>> iterator = this.getSet().iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    @Override
    public boolean contains(Holder<Biome> holder) {
        return this.getSet().contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<Biome> owner) {
        return true;
    }

    @Override
    public Optional<TagKey<Biome>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public List<HolderSet<Biome>> getIncludeComponents() {
        return this.includeComponents;
    }

    @Override
    public List<HolderSet<Biome>> getExcludeComponents() {
        return this.excludeComponents;
    }

    @Override
    public void addHolderSet(List<HolderSet<Biome>> components, HolderSet<Biome> holderSet) {
        components.add(holderSet);
    }

    @Override
    public void addDelayedHolderSet(List<HolderSet<Biome>> components, Supplier<HolderSet<Biome>> holderSetSupplier) {
        components.add(new DelayedHolderSet<>(holderSetSupplier));
    }

    @Override
    public void addNameRegexMatch(List<HolderSet<Biome>> components, Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex) {
        Supplier<HolderSet<Biome>> sup = () -> new NameRegexMatchHolderSet<>(registryLookup.get(), regex);
        addDelayedHolderSet(components, sup);
    }

    @Override
    public void addTagsRegexMatch(List<HolderSet<Biome>> components, Supplier<HolderLookup.RegistryLookup<Biome>> registryLookup, String regex) {
        Supplier<HolderSet<Biome>> sup = () -> new TagsRegexMatchHolderSet<>(registryLookup.get(), regex);
        addDelayedHolderSet(components, sup);
    }

    @Override
    public void addOr(List<HolderSet<Biome>> components, List<HolderSet<Biome>> values) {
        addHolderSet(components, new OrHolderSet<>(values));
    }

    @Override
    public void clear() {
        this.includeComponents.clear();
        this.excludeComponents.clear();
    }

    @NotNull
    @Override
    public Iterator<Holder<Biome>> iterator() {
        return this.getSet().iterator();
    }
}