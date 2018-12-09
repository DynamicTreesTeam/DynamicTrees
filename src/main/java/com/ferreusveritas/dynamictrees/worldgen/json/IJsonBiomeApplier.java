package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.biome.Biome;

public interface IJsonBiomeApplier {
	
	public void apply(BiomeDataBase dbase, JsonElement element, Biome biome);
	
	public default Operation readMethod(JsonObject object) {
		JsonElement method = object.get("method");
		if(method != null && method.isJsonPrimitive() && method.getAsJsonPrimitive().isString()) {
			String methodName = method.getAsJsonPrimitive().getAsString();
			
			if("replace".equals(methodName)) {
				return Operation.REPLACE;
			}
			if("before".equals(methodName)) {
				return Operation.SPLICE_BEFORE;
			}
			if("after".equals(methodName)) {
				return Operation.SPLICE_AFTER;
			}
		}
		
		return Operation.REPLACE;
	}
	
	
}
