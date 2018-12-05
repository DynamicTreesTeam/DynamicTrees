package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.IDensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ISpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.RandomSpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.StaticSpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeDataBasePopulatorJson implements IBiomeDataBasePopulator {

	private final String DEFAULT = "...";
	private JsonElement jsonElement;
	
	public BiomeDataBasePopulatorJson(ResourceLocation jsonLocation) {
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(jsonLocation).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			jsonElement = new Gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BiomeDataBasePopulatorJson(JsonElement jsonElement) {
		this.jsonElement = jsonElement;
	}
	
	@Override
	public void populate(BiomeDataBase biomeDataBase) {
		if(jsonElement != null && jsonElement.isJsonArray()) {
			for(JsonElement sectionElement : jsonElement.getAsJsonArray()) {
				if(sectionElement.isJsonObject()) {
					JsonObject section = sectionElement.getAsJsonObject();
					readSection(section, biomeDataBase);
				}
			}
		}
	}
	
	public static boolean biomeHasTypes(Biome biome, List<BiomeDictionary.Type> types) {
		return types.stream().allMatch(t -> BiomeDictionary.hasType(biome, t));
	}
	
	private void readSection(JsonObject section, BiomeDataBase dbase) {

		List<Biome> biomeList = Lists.newArrayList(Biome.REGISTRY);
		
		JsonElement biomeElement = section.get("biome");
		if(biomeElement != null && biomeElement.isJsonPrimitive()) {
			JsonPrimitive primitive = biomeElement.getAsJsonPrimitive();
			if(primitive.isString()) {
				String biomeMatch = primitive.getAsString();
				biomeList = biomeList.stream().filter(b -> b.getRegistryName().toString().matches(biomeMatch)).collect(Collectors.toList());
			}
		}

		JsonElement typeElement = section.get("type");
		if(typeElement != null) {
			if (typeElement.isJsonPrimitive() && typeElement.getAsJsonPrimitive().isString()) {
				String typeMatch = typeElement.getAsString();
				List<BiomeDictionary.Type> types = Arrays.asList(typeMatch.split(",")).stream().map(BiomeDictionary.Type::getType).collect(Collectors.toList());
				biomeList = biomeList.stream().filter(b -> biomeHasTypes(b, types)).collect(Collectors.toList());
			} else 
			if (typeElement.isJsonArray()) {
				List<BiomeDictionary.Type> types = new ArrayList<>();
				for(JsonElement element : typeElement.getAsJsonArray()) {
					if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
						types.add(BiomeDictionary.Type.getType(element.getAsString()));
					}
				}
				biomeList = biomeList.stream().filter(b -> biomeHasTypes(b, types)).collect(Collectors.toList());
			}
		}
		
		for(Biome biome: biomeList) {
			for(Entry<String, JsonElement> entry : section.entrySet()) {
				String entryName = entry.getKey();
				JsonElement element = entry.getValue();
				
				if("species".equals(entryName)) {
					if(element.isJsonObject()) {
						JsonObject object = element.getAsJsonObject();
						Operation operation = readMethod(object);
						ISpeciesSelector speciesSelector = readSpeciesSelector(object);
						if(speciesSelector != null) {
							dbase.setSpeciesSelector(biome, speciesSelector, operation);
						}
					}
				}
				else if("density".equals(entryName)) {
					if(element.isJsonObject()) {
						JsonObject object = element.getAsJsonObject();
						Operation operation = readMethod(object);
						IDensitySelector densitySelector = readDensitySelector(object);
						if(densitySelector != null) {
							dbase.setDensitySelector(biome, densitySelector, operation);
						}
					}
				}
				else if("chance".equals(entryName)) {
					if(element.isJsonObject()) {
						JsonObject object = element.getAsJsonObject();
						Operation operation = readMethod(object);
						IChanceSelector chanceSelector = readChanceSelector(object);
						if(chanceSelector != null) {
							dbase.setChanceSelector(biome, chanceSelector, operation);
						}
					}
				}
			}
		}
	}
	


	private Operation readMethod(JsonObject object) {
		JsonElement method = object.get("method");
		if(method.isJsonPrimitive() && method.getAsJsonPrimitive().isString()) {
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
	
	private ISpeciesSelector readSpeciesSelector(JsonObject mainObject) {
		
		JsonElement randomElement = mainObject.get("random");
		if(randomElement != null && randomElement.isJsonObject()) {
			RandomSpeciesSelector rand = new RandomSpeciesSelector();
			for(Entry<String, JsonElement> entry : randomElement.getAsJsonObject().entrySet()) {
				String speciesName = entry.getKey();
				JsonElement speciesElement = entry.getValue();
				int weight = 0;
				if(speciesElement.isJsonPrimitive() && speciesElement.getAsJsonPrimitive().isNumber()) {
					weight = speciesElement.getAsJsonPrimitive().getAsInt();
					if(weight > 0) {
						if(DEFAULT.equals(speciesName)) {
							rand.add(weight);
						} else {
							Species species = TreeRegistry.findSpeciesSloppy(speciesName);
							if(species != Species.NULLSPECIES) {
								rand.add(species, weight);
							}
						}
					}
				}
			}
			
			if(rand.getSize() > 0) {
				return rand;
			}
		}
		
		JsonElement staticElement = mainObject.get("static");
		if(staticElement != null && staticElement.isJsonPrimitive() && staticElement.getAsJsonPrimitive().isString()) {
			String speciesName = staticElement.getAsJsonPrimitive().getAsString();
			if(DEFAULT.equals(speciesName)) {
				return new StaticSpeciesSelector();
			}
			Species species = TreeRegistry.findSpeciesSloppy(speciesName);
			if(species != Species.NULLSPECIES) {
				return new StaticSpeciesSelector(new SpeciesSelection(species));
			}
		}

		return null;
	}
	
	private IChanceSelector readChanceSelector(JsonObject mainObject) {
		
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
				if(DEFAULT.equals(value)) {
					return (rnd, spc, rad) -> EnumChance.UNHANDLED;
				}
			}
		}
		
		JsonElement mathElement = mainObject.get("math");
		if(mathElement != null) {
			JsonMath m = new JsonMath(mathElement);
			return (rnd, spc, rad) -> rnd.nextFloat() < m.apply(rnd, spc, rad) ? EnumChance.OK : EnumChance.CANCEL;
		}
		
		return null;
	}
	
	private IDensitySelector readDensitySelector(JsonObject mainObject) {
		
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
			JsonMath m = new JsonMath(mathElement);
			return (rnd, n) -> m.apply(rnd, (float) n);
		}
		
		return null;
	}
	

}
