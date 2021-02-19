package com.ferreusveritas.dynamictrees.worldgen.json;

import com.google.gson.JsonElement;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface IJsonBiomeSelector {
	
	Predicate<Biome> getFilter(@Nullable final JsonElement jsonElement);
	
}
