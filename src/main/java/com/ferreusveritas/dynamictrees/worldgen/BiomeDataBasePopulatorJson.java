package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeApplier;
import com.ferreusveritas.dynamictrees.worldgen.json.IJsonBiomeSelector;
import com.ferreusveritas.dynamictrees.worldgen.json.JsonBiomePropertyApplierChance;
import com.ferreusveritas.dynamictrees.worldgen.json.JsonBiomePropertyApplierDensity;
import com.ferreusveritas.dynamictrees.worldgen.json.JsonBiomePropertyApplierSpecies;
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

	public static final String DEFAULT = "...";
	
	public static final String SPECIES = "species";
	public static final String DENSITY = "density";
	public static final String CHANCE = "chance";

	public static final String CANCELVANILLA = "cancelvanilla";
	public static final String MULTIPASS = "multipass";
	public static final String SUBTERRANEAN = "subterranean";
	
	public static final String SELECT = "select";
	public static final String APPLY = "apply";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	
	private JsonElement jsonElement;
	
	private static Map<String, IJsonBiomeSelector> jsonBiomeSelectorMap = new HashMap<>();
	private static Map<String, IJsonBiomeApplier> jsonBiomeApplierMap = new HashMap<>(); 
	
	static {
		
		jsonBiomeApplierMap.put(SPECIES, new JsonBiomePropertyApplierSpecies());
		
		jsonBiomeApplierMap.put(DENSITY, new JsonBiomePropertyApplierDensity());

		jsonBiomeApplierMap.put(CHANCE, new JsonBiomePropertyApplierChance());
		
		jsonBiomeApplierMap.put(CANCELVANILLA, (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean cancel = element.getAsBoolean();
				dbase.setCancelVanillaTreeGen(biome, cancel);
			}
		});
		
		jsonBiomeApplierMap.put(MULTIPASS, (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean multipass = element.getAsBoolean();
				
				if(multipass) {
					//Enable poisson disc multipass of roofed forests to ensure maximum density even with large trees
					//by filling in gaps in the generation with smaller trees 
					dbase.setMultipass(biome, pass -> {
						switch(pass) {
							case 0: return 0;//Zero means to run as normal
							case 1: return 5;//Return only radius 5 on pass 1
							case 2: return 3;//Return only radius 3 on pass 2
							default: return -1;//A negative number means to terminate
						}
					});
				}
			}
		});
		
		jsonBiomeApplierMap.put(SUBTERRANEAN,  (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean subterranean = element.getAsBoolean();
				dbase.setIsSubterranean(biome, subterranean);
			}
		});
		
		
		
		jsonBiomeSelectorMap.put(NAME, jsonElement -> {
			if(jsonElement != null && jsonElement.isJsonPrimitive()) {
				JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
				if(primitive.isString()) {
					String biomeMatch = primitive.getAsString();
					return b-> b.getRegistryName().toString().matches(biomeMatch);
				}
			}
			
			return b -> false;
		});
		
		jsonBiomeSelectorMap.put(TYPE, jsonElement -> {
			if(jsonElement != null) {
				if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
					String typeMatch = jsonElement.getAsString();
					List<BiomeDictionary.Type> types = Arrays.asList(typeMatch.split(",")).stream().map(BiomeDictionary.Type::getType).collect(Collectors.toList());
					return b -> biomeHasTypes(b, types);
				} else 
				if (jsonElement.isJsonArray()) {
					List<BiomeDictionary.Type> types = new ArrayList<>();
					for(JsonElement element : jsonElement.getAsJsonArray()) {
						if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
							types.add(BiomeDictionary.Type.getType(element.getAsString()));
						}
					}
					return b -> biomeHasTypes(b, types);
				}
			}
						
			return b -> false;
		});
		
	}
	
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
	
	private class JsonBiomeSelectorData {
		final IJsonBiomeSelector selector;
		final JsonElement elementData;
		
		JsonBiomeSelectorData(IJsonBiomeSelector selector, JsonElement elementData) {
			this.selector = selector;
			this.elementData = elementData;
		}
		
		Predicate<Biome> getFilter() {
			return this.selector.getFilter(elementData);
		}
	}

	private class JsonBiomeApplierData {
		IJsonBiomeApplier applier;
		JsonElement elementData;
		
		JsonBiomeApplierData(IJsonBiomeApplier applier, JsonElement elementData) {
			this.applier = applier;
			this.elementData = elementData;
		}
		
		void apply(BiomeDataBase dbase, Biome biome) {
			this.applier.apply(dbase, elementData, biome);
		}
	}
	
	public static boolean isComment(String s) {
		return s.startsWith("__");//Allow for comments.  Comments are anything starting with "__"
	}
	
	private void readSection(JsonObject section, BiomeDataBase dbase) {
		
		List<JsonBiomeSelectorData> selectors = new LinkedList<>();
		List<JsonBiomeApplierData> appliers = new LinkedList<>();
		
		for(Entry<String, JsonElement> entry : section.entrySet()) {
			
			String key = entry.getKey();
			JsonElement element = entry.getValue();
			
			if(!isComment(key)) {
				if(SELECT.equals(key)) {
					if(element.isJsonObject()) {
						for(Entry<String, JsonElement> selectElement : element.getAsJsonObject().entrySet()) {
							String selectorName = selectElement.getKey();
							if(!isComment(selectorName)) {
								IJsonBiomeSelector selector = jsonBiomeSelectorMap.get(selectorName);
								if(selector != null) {
									selectors.add(new JsonBiomeSelectorData(selector, selectElement.getValue()));
								} else {
									System.err.println("Json Error: Undefined selector property \"" + selectorName + "\"");
								}
							}
						}
					}
				}
				else 
				if(APPLY.equals(key)) {
					if(element.isJsonObject()) {
						for(Entry<String, JsonElement> selectElement : element.getAsJsonObject().entrySet()) {
							String applierName = selectElement.getKey();
							if(!isComment(applierName)) {
								IJsonBiomeApplier applier = jsonBiomeApplierMap.get(applierName);
								if(applier != null) {
									appliers.add(new JsonBiomeApplierData(applier, selectElement.getValue()));
								} else {
									System.err.println("Json Error: Undefined applier property \"" + applierName + "\"");
								}
							}
						}
					}
				}
				else {
					System.err.println("Json Error: Undefined operation \"" + key + "\"");
				}
			}
			
		}
		
		//Filter biomes by selector predicates
		Stream<Biome> stream = Lists.newArrayList(Biome.REGISTRY).stream();
		for(JsonBiomeSelectorData s : selectors) {
			stream = stream.filter(s.getFilter());
		}
		
		//Apply all of the applicator to the database
		stream.forEach( biome -> {
			appliers.forEach( a -> a.apply(dbase, biome) );
		});
	}
	
}
