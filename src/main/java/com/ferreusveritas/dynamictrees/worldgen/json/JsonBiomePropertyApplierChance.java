package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IChanceSelector;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.biome.Biome;

public class JsonBiomePropertyApplierChance implements IJsonBiomeApplier {
	
	@Override
	public void apply(BiomeDataBase dbase, JsonElement element, Biome biome) {
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			Operation operation = readMethod(object);
			IChanceSelector chanceSelector = readChanceSelector(object, biome);
			if(chanceSelector != null) {
				dbase.setChanceSelector(biome, chanceSelector, operation);
			}
		}
	}
	
	private IChanceSelector readChanceSelector(JsonObject mainObject, Biome biome) {
		
		JsonElement staticElement = mainObject.get("static");
		if(staticElement != null && staticElement.isJsonPrimitive()) {
			if(staticElement.getAsJsonPrimitive().isNumber()) {
				float value = staticElement.getAsJsonPrimitive().getAsFloat();
				if(value <= 0) {
					return (rnd, spc, rad) -> EnumChance.CANCEL;
				}
				if(value >= 1) {
					return (rnd, spc, rad) -> EnumChance.OK;
				}
				return (rnd, spc, rad) -> rnd.nextFloat() < value ? EnumChance.OK : EnumChance.CANCEL;
			}
			if(staticElement.getAsJsonPrimitive().isString()) {
				String value = staticElement.getAsString();
				if(BiomeDataBasePopulatorJson.DEFAULT.equals(value)) {
					return (rnd, spc, rad) -> EnumChance.UNHANDLED;
				}
			}
		}
		
		JsonElement mathElement = mainObject.get("math");
		if(mathElement != null) {
			JsonMath m = new JsonMath(mathElement, biome);
			return (rnd, spc, rad) -> rnd.nextFloat() < m.apply(rnd, spc, rad) ? EnumChance.OK : EnumChance.CANCEL;
		}
		
		return null;
	}
	
}
