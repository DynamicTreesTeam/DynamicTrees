package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Harley O'Connor
 */
public final class BiomeList extends LinkedList<Biome> {

    public BiomeList() {
    }

    public BiomeList(Collection<? extends Biome> c) {
        super(c);
    }

    public static BiomeList getAll() {
        final BiomeList biomes = new BiomeList();
        biomes.addAll(ForgeRegistries.BIOMES.getValues());
        return biomes;
    }

}