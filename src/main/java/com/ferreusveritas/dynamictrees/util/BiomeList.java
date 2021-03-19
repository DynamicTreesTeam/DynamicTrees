package com.ferreusveritas.dynamictrees.util;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

/**
 * @author Harley O'Connor
 */
public final class BiomeList extends ArrayList<Biome> {

    public static BiomeList getAll () {
        final BiomeList biomes = new BiomeList();
        biomes.addAll(ForgeRegistries.BIOMES.getValues());
        return biomes;
    }

}