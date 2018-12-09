package com.ferreusveritas.dynamictrees.worldgen.json;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IDensitySelector;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.biome.Biome;

public class JsonBiomePropertyApplierDensity implements IJsonBiomeApplier {
	
	@Override
	public void apply(BiomeDataBase dbase, JsonElement element, Biome biome) {
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			Operation operation = readMethod(object);
			IDensitySelector densitySelector = readDensitySelector(object, biome);
			if(densitySelector != null) {
				dbase.setDensitySelector(biome, densitySelector, operation);
			}
		}
	}
	
	private static IDensitySelector readDensitySelector(JsonObject mainObject, Biome biome) {
		
		JsonElement scaleElement = mainObject.get("scale");
		if(scaleElement != null && scaleElement.isJsonArray()) {
			List<Float> parameters = new ArrayList<>();
			for(JsonElement element : scaleElement.getAsJsonArray()) {
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
