package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IChanceSelector;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.world.biome.Biome;

public class JsonBiomePropertyApplierChance implements IJsonBiomeApplier {
	
	@Override
	public void apply(BiomeDataBase dbase, JsonElement element, Biome biome) {
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			dbase.setChanceSelector(biome, readChanceSelector(object, biome), readMethod(object));
		}
		else if(element.isJsonPrimitive()) {
			JsonPrimitive prim = element.getAsJsonPrimitive();
			if(prim.isNumber()) {
				dbase.setChanceSelector(biome, createSimpleChanceSelector(prim.getAsFloat()), Operation.REPLACE);
			}
			else if(prim.isString()) {
				String simple = prim.getAsString();
				if("standard".equals(simple)) {
					//Start dropping tree spawn opportunities when the radius gets bigger than 3
					dbase.setChanceSelector(biome, (rnd, spc, rad) -> rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ? EnumChance.OK : EnumChance.CANCEL, Operation.REPLACE); 
				}
			}
		}
	}
	
	private IChanceSelector createSimpleChanceSelector(float value) {
		if(value <= 0) {
			return (rnd, spc, rad) -> EnumChance.CANCEL;
		}
		if(value >= 1) {
			return (rnd, spc, rad) -> EnumChance.OK;
		}
		return (rnd, spc, rad) -> rnd.nextFloat() < value ? EnumChance.OK : EnumChance.CANCEL;
	}
	
	private IChanceSelector readChanceSelector(JsonObject mainObject, Biome biome) {
		
		JsonElement staticElement = mainObject.get("static");
		if(staticElement != null && staticElement.isJsonPrimitive()) {
			if(staticElement.getAsJsonPrimitive().isNumber()) {
				return createSimpleChanceSelector(staticElement.getAsJsonPrimitive().getAsFloat());
			}
			if(staticElement.getAsJsonPrimitive().isString()) {
				String value = staticElement.getAsString();
				if(isDefault(value)) {
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
