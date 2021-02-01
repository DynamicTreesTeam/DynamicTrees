package com.ferreusveritas.dynamictrees.worldgen;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.ferreusveritas.dynamictrees.worldgen.json.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeDataBasePopulatorJson extends BiomeSelectorJson implements IBiomeDataBasePopulator {
	
	public static final String SPECIES = "species";
	public static final String DENSITY = "density";
	public static final String CHANCE = "chance";
	
	public static final String MULTIPASS = "multipass";
	public static final String SUBTERRANEAN = "subterranean";
	public static final String FORESTNESS = "forestness";
	public static final String BLACKLIST = "blacklist";
	public static final String RESET = "reset";

	public static final String WHITE = "white";
	public static final String APPLY = "apply";

	private final JsonElement jsonElement;
	private final String fileName;

	private static Map<String, IJsonBiomeApplier> jsonBiomeApplierMap = new HashMap<>();

	public static Set<Biome> blacklistedBiomes = new HashSet<>();

	public static void addJsonBiomeApplier(String name, IJsonBiomeApplier applier) {
		jsonBiomeApplierMap.put(name, applier);
	}

	public static void cleanup() {
		cleanupBiomeSelectors();
		jsonBiomeApplierMap = new HashMap<>();
		blacklistedBiomes = new HashSet<>();
	}
	
	public static void registerJsonCapabilities(WorldGenRegistry.BiomeDataBaseJsonCapabilityRegistryEvent event) {

		registerJsonBiomeCapabilities(event); // Registers biome selector capabilities.

		event.register(SPECIES, new JsonBiomePropertyApplierSpecies());

		event.register(DENSITY, new JsonBiomePropertyApplierDensity());

		event.register(CHANCE, new JsonBiomePropertyApplierChance());

		event.register(MULTIPASS, (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean multipass = element.getAsBoolean();

				if(multipass) {
					DynamicTrees.getLogger().debug("Biome set for multipass: " + biome);

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

		event.register(SUBTERRANEAN,  (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean subterranean = element.getAsBoolean();
				DynamicTrees.getLogger().debug("Biome set to subterranean: " + biome);
				dbase.setIsSubterranean(biome, subterranean);
			}
		});

		event.register(FORESTNESS, (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				float forestness = element.getAsFloat();
				DynamicTrees.getLogger().debug("Forestness set for biome: " + biome + " at " + forestness);
				dbase.setForestness(biome, forestness);
			}
		});

		event.register(BLACKLIST, (dbase, element, biome) -> {
			if(element.isJsonPrimitive()) {
				boolean blacklist = element.getAsBoolean();
				if(blacklist) {
					DynamicTrees.getLogger().debug("Blacklisted biome: " + biome);
					blacklistedBiomes.add(biome);
				} else {
					blacklistedBiomes.remove(biome);
				}
			}
		});

		event.register(RESET, (dbase, element, biome) -> {
			dbase.setCancelVanillaTreeGen(biome, false);
			dbase.setSpeciesSelector(biome, (pos, dirt, rnd) -> new BiomePropertySelectors.SpeciesSelection(), BiomeDataBase.Operation.REPLACE);
			dbase.setDensitySelector(biome, (rnd, nd) -> -1, BiomeDataBase.Operation.REPLACE);
			dbase.setChanceSelector(biome, (rnd, spc, rad) -> BiomePropertySelectors.EnumChance.UNHANDLED, BiomeDataBase.Operation.REPLACE);
			dbase.setForestness(biome, 0.0f);
			dbase.setIsSubterranean(biome, false);
			dbase.setMultipass(biome, pass -> (pass == 0 ? 0 : -1));
		});

	}
	
	public BiomeDataBasePopulatorJson(ResourceLocation jsonLocation) {
		this(JsonHelper.load(jsonLocation), "default.json");
	}
	
	public BiomeDataBasePopulatorJson(JsonElement jsonElement, String fileName) {
		this.jsonElement = jsonElement;
		this.fileName = fileName;
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

	private static class JsonBiomeApplierData {
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

	private void readSection(JsonObject section, BiomeDataBase dbase) {
		
		List<JsonBiomeSelectorData> selectors = new LinkedList<>();
		List<JsonBiomeApplierData> appliers = new LinkedList<>();
		
		for(Entry<String, JsonElement> entry : section.entrySet()) {
			
			String key = entry.getKey();
			JsonElement element = entry.getValue();
			
			if(!isComment(key)) {
				List<JsonBiomeSelectorData> entrySelectors = this.readSelection(key, element, this.fileName);

				if (entrySelectors.size() > 0)
					selectors.addAll(entrySelectors);
				else if (WHITE.equals(key)) {
					if(element.isJsonPrimitive()) {
						if("all".equals(element.getAsString())) {
							blacklistedBiomes.clear();
						}
					}
				} else if (APPLY.equals(key)) {
					if(element.isJsonObject()) {
						for(Entry<String, JsonElement> selectElement : element.getAsJsonObject().entrySet()) {
							String applierName = selectElement.getKey();
							if(!isComment(applierName)) {
								IJsonBiomeApplier applier = jsonBiomeApplierMap.get(applierName);
								if(applier != null) {
									appliers.add(new JsonBiomeApplierData(applier, selectElement.getValue()));
								} else {
									DynamicTrees.getLogger().error("Json Error: Undefined applier property \"" + applierName + "\" in " + this.fileName + ".");
								}
							}
						}
					}
				} else {
					DynamicTrees.getLogger().error("Json Error: Undefined operation \"" + key + "\" in " + this.fileName + ".");
				}
			}
			
		}
		
		//Filter biomes by selector predicates
		Stream<Biome> stream = Lists.newArrayList(ForgeRegistries.BIOMES).stream();
		for(JsonBiomeSelectorData s : selectors) {
			stream = stream.filter(s.getFilter());
		}
		
		//Filter blacklisted biomes
		stream = stream.filter(b -> !blacklistedBiomes.contains(b));
		
		//Apply all of the applicators to the database
		stream.forEach( biome -> {
			appliers.forEach( a -> a.apply(dbase, biome) );
		});
	}

}
