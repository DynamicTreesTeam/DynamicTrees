package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ISpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.RandomSpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.StaticSpeciesSelector;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.biome.Biome;

import java.util.Map.Entry;

public class JsonBiomePropertyApplierSpecies implements IJsonBiomeApplier {
	
	@Override
	public void apply(BiomeDataBase dbase, JsonElement element, Biome biome) {
		if(element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			dbase.setSpeciesSelector(biome, readSpeciesSelector(object), readMethod(object));
		}
		else if(element.isJsonPrimitive()) {
			dbase.setSpeciesSelector(biome, createStaticSpeciesSelector(element.getAsString()), Operation.REPLACE);
		}
	};

	private ISpeciesSelector createStaticSpeciesSelector(String speciesName) {
//		if(isDefault(speciesName)) {
//			return new StaticSpeciesSelector();
//		}
//		Species species = TreeRegistry.findSpeciesSloppy(speciesName);
//		if(species != Species.NULLSPECIES) {
//			return new StaticSpeciesSelector(new SpeciesSelection(species));
//		}
//
		return null;
	}
	
	private ISpeciesSelector readSpeciesSelector(JsonObject mainObject) {

//		JsonElement staticElement = mainObject.get("static");
//		if(staticElement != null && staticElement.isJsonPrimitive()) {
//			return createStaticSpeciesSelector(staticElement.getAsString());
//		}
//
//		JsonElement randomElement = mainObject.get("random");
//		if(randomElement != null && randomElement.isJsonObject()) {
//			RandomSpeciesSelector rand = new RandomSpeciesSelector();
//			for(Entry<String, JsonElement> entry : randomElement.getAsJsonObject().entrySet()) {
//				String speciesName = entry.getKey();
//				JsonElement speciesElement = entry.getValue();
//				int weight = 0;
//				if(speciesElement.isJsonPrimitive() && speciesElement.getAsJsonPrimitive().isNumber()) {
//					weight = speciesElement.getAsJsonPrimitive().getAsInt();
//					if(weight > 0) {
//						if(isDefault(speciesName)) {
//							rand.add(weight);
//						} else {
//							Species species = TreeRegistry.findSpeciesSloppy(speciesName);
//							if(species != Species.NULLSPECIES) {
//								rand.add(species, weight);
//							}
//						}
//					}
//				}
//			}
//
//			if(rand.getSize() > 0) {
//				return rand;
//			}
//		}
//
		return null;
	}
	
}
