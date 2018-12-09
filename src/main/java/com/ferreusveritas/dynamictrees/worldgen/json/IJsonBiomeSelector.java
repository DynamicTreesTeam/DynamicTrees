package com.ferreusveritas.dynamictrees.worldgen.json;

import java.util.function.Predicate;

import com.google.gson.JsonElement;

import net.minecraft.world.biome.Biome;

public interface IJsonBiomeSelector {
	
	Predicate<Biome> getFilter(JsonElement jsonElement);
	
}
