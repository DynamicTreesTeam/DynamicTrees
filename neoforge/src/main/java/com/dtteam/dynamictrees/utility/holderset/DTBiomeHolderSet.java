package com.dtteam.dynamictrees.utility.holderset;

import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class DTBiomeHolderSet extends IncludesExcludesHolderSet<Biome> implements IDTBiomeHolderSet {
    @Nullable
    private Set<ResourceKey<Biome>> keys = null;

    public DTBiomeHolderSet() {
        super(new AndHolderSet<>(new ArrayList<>()), new OrHolderSet<>(new ArrayList<>()));
        this.addInvalidationListener(() -> this.keys = null);
    }

    public boolean containsKey(ResourceKey<Biome> biomeKey) {
        if (this.keys == null) {
            this.keys = new HashSet<>();
            for (Holder<Biome> holder : this.getSet()) {
                this.keys.add(holder.unwrapKey().orElseThrow());
            }
        }

        return this.keys.contains(biomeKey);
    }

    @Override
    public void addHolderSet(List<HolderSet<Biome>> components, HolderSet<Biome> holderSetSupplier) {
        components.add(holderSetSupplier);
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

}