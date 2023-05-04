package com.ferreusveritas.dynamictrees.util.holderset;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.holdersets.AndHolderSet;

import java.util.ArrayList;
import java.util.List;

public class DTBiomeHolderSet extends IncludesExcludesHolderSet<Biome> {
    public DTBiomeHolderSet() {
        super(new AndHolderSet<>(new ArrayList<>()), new AndHolderSet<>(new ArrayList<>()));
    }
    public boolean includesBiome(Holder<Biome> biome){

        return includes().stream().anyMatch(holder -> {
            ResourceKey<Biome> holderKey = holder.unwrapKey().get();
            ResourceKey<Biome> biomeKey = biome.unwrapKey().get();
            return holderKey.equals(biomeKey);
        });
    }
}