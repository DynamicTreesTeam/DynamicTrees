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
        // TODO: ForgeRegistries.BIOMES does not contain any biomes declared in datapacks. But we don't have a world yet. Anything we can do? -SizableShrimp
        biomes.addAll(ForgeRegistries.BIOMES.getValues());
        return biomes;
    }

}