package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.biome.Biome;

public interface IJsonBiomeApplier {

	String DEFAULT = "...";
	String REPLACE = "replace";
	String BEFORE = "before";
	String AFTER = "after";

	void apply(BiomeDataBase dbase, JsonElement element, Biome biome);

	default Operation readMethod(JsonObject object) {
		JsonElement method = object.get("method");
		if (method != null && method.isJsonPrimitive() && method.getAsJsonPrimitive().isString()) {
			String methodName = method.getAsJsonPrimitive().getAsString();

			if (REPLACE.equals(methodName)) {
				return Operation.REPLACE;
			}
			if (BEFORE.equals(methodName)) {
				return Operation.SPLICE_BEFORE;
			}
			if (AFTER.equals(methodName)) {
				return Operation.SPLICE_AFTER;
			}
		}

		return Operation.REPLACE;
	}

	default boolean isDefault(String candidate) {
		return DEFAULT.equals(candidate);
	}

}
