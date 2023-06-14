package com.ferreusveritas.dynamictrees.util.holderset;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DTBiomeHolderSet extends IncludesExcludesHolderSet<Biome> {
    @Nullable
    private Set<ResourceKey<Biome>> keys = null;

    public DTBiomeHolderSet() {
        super(new AndHolderSet<>(new ArrayList<>()), new AndHolderSet<>(new ArrayList<>()));
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
}