package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IDensitySelector;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase.Operation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class JsonBiomePropertyApplierDensity implements IJsonBiomeApplier {
	
	@Override
	public void apply(BiomeDatabase dbase, JsonElement element, Biome biome) {
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			Operation operation = readMethod(object);
			dbase.setDensitySelector(biome, readDensitySelector(object, biome), operation);
		}
		else if(element.isJsonArray()) {
			dbase.setDensitySelector(biome, createScaleDensitySelector(element.getAsJsonArray()), Operation.REPLACE);
		}
		else if(element.isJsonPrimitive()) {
			JsonPrimitive prim = element.getAsJsonPrimitive();
			if(prim.isNumber()) {
				float value = prim.getAsFloat();
				dbase.setDensitySelector(biome, (rnd, n) -> value, Operation.REPLACE);
			}
			else if(prim.isString()) {
				String simple = prim.getAsString();
				if("standard".equals(simple)) {
					final double treeDensity = MathHelper.clamp(0.5f, 0.0f, 1.0f); // biome.decorator.treesPerChunk / 10.0f
					dbase.setDensitySelector(biome, (rnd, n) -> n * treeDensity, Operation.REPLACE );
				}
			}
		}
	}
	
	private static IDensitySelector createScaleDensitySelector(JsonArray jsonArray) {
		if(jsonArray != null && jsonArray.isJsonArray()) {
			List<Float> parameters = new ArrayList<>();
			for(JsonElement element : jsonArray) {
				if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
					parameters.add(element.getAsJsonPrimitive().getAsFloat());
				}
			}
			switch(parameters.size()) {
				case 0: return (rnd, n) -> n;
				case 1: return (rnd, n) -> n * parameters.get(0);
				case 2: return (rnd, n) -> (n * parameters.get(0)) + parameters.get(1);
				case 3: return (rnd, n) -> ((n * parameters.get(0)) + parameters.get(1)) * parameters.get(2);
				default: return (rnd, n) -> 0.0f;
			}
		}
		
		return (rnd, n) -> n;
	}
	
	private static IDensitySelector readDensitySelector(JsonObject mainObject, Biome biome) {
		
		JsonElement scaleElement = mainObject.get("scale");
		if(scaleElement != null && scaleElement.isJsonArray()) {
			return createScaleDensitySelector(scaleElement.getAsJsonArray());
		}
		
		JsonElement staticElement = mainObject.get("static");
		if(staticElement != null && staticElement.isJsonPrimitive() && staticElement.getAsJsonPrimitive().isNumber()) {
			return (rnd, n) -> staticElement.getAsJsonPrimitive().getAsFloat();
		}
		
		JsonElement mathElement = mainObject.get("math");
		if(mathElement != null) {
			JsonMath m = new JsonMath(mathElement, biome);
			return (rnd, n) -> m.apply(rnd, (float) n);
		}
		
		return null;
	}
	
}