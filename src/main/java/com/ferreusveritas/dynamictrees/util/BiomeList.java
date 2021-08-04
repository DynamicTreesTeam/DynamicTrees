package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Harley O'Connor
 */
public final class BiomeList extends ArrayList<Biome> {

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