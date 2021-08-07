package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry.BiomeDataBaseJsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.Operation;
import com.ferreusveritas.dynamictrees.worldgen.json.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeDataBasePopulatorJson implements IBiomeDataBasePopulator {

	public static final String SPECIES = "species";
	public static final String DENSITY = "density";
	public static final String CHANCE = "chance";

	public static final String CANCELVANILLA = "cancelvanilla";
	public static final String MULTIPASS = "multipass";
	public static final String SUBTERRANEAN = "subterranean";
	public static final String FORESTNESS = "forestness";
	public static final String BLACKLIST = "blacklist";
	public static final String RESET = "reset";

	public static final String WHITE = "white";
	public static final String SELECT = "select";
	public static final String APPLY = "apply";
	public static final String NAME = "name";
	public static final String TYPE = "type";

	private final JsonElement jsonElement;

	private static Map<String, IJsonBiomeSelector> jsonBiomeSelectorMap = new HashMap<>();
	private static Map<String, IJsonBiomeApplier> jsonBiomeApplierMap = new HashMap<>();

	public static Set<Biome> blacklistedBiomes = new HashSet<>();

	public static void addJsonBiomeSelector(String name, IJsonBiomeSelector selector) {
		jsonBiomeSelectorMap.put(name, selector);
	}

	public static void addJsonBiomeApplier(String name, IJsonBiomeApplier applier) {
		jsonBiomeApplierMap.put(name, applier);
	}

	public static void cleanup() {
		jsonBiomeApplierMap = new HashMap<>();
		jsonBiomeSelectorMap = new HashMap<>();
		blacklistedBiomes = new HashSet<>();
	}

	public static void registerJsonCapabilities(BiomeDataBaseJsonCapabilityRegistryEvent event) {

		event.register(NAME, jsonElement -> {
			if (jsonElement == null) {
				return b -> false;
			}

			if (jsonElement.isJsonPrimitive()) {
				JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
				if (primitive.isString()) {
					String biomeMatch = primitive.getAsString();
					if (biomeMatch.charAt(0) == '!') {
						return biome -> !String.valueOf(biome.getRegistryName()).matches(biomeMatch.substring(1));
					} else {
						return biome -> String.valueOf(biome.getRegistryName()).matches(biomeMatch);
					}
				}
			} else if (jsonElement.isJsonArray()) {
				final List<String> namesWhitelist = new ArrayList<>();
				final List<String> namesBlacklist = new ArrayList<>();

				for (final JsonElement nameElement : jsonElement.getAsJsonArray()) {
					if (!nameElement.isJsonPrimitive()) {
						continue;
					}
					final JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
					if (!primitive.isString()) {
						continue;
					}
					final String biomeMatch = primitive.getAsString();
					if (biomeMatch.charAt(0) == '!') {
						namesBlacklist.add(biomeMatch.substring(1));
					} else {
						namesWhitelist.add(biomeMatch);
					}
				}

				return biome -> {
					final String regName = String.valueOf(biome.getRegistryName());

					// Accept if the biome name matches any from the whilstlist and none from the blacklist. 
					return namesWhitelist.stream().anyMatch(regName::matches) &&
						namesBlacklist.stream().noneMatch(regName::matches);
				};
			}

			return b -> false;
		});

		event.register(TYPE, jsonElement -> {
			if (jsonElement != null) {
				List<BiomeDictionary.Type> typesWhitelist = new ArrayList<>();
				List<BiomeDictionary.Type> typesBlacklist = new ArrayList<>();

				if (jsonElement.isJsonPrimitive()) {
					String typeMatch = jsonElement.getAsString();
					List<String> matches = Arrays.stream(typeMatch.split(",")).collect(Collectors.toList());
					for (String match : matches) {
						if (match.charAt(0) == '!') {
							typesBlacklist.add(BiomeDictionary.Type.getType(match.substring(1)));
						} else {
							typesWhitelist.add(BiomeDictionary.Type.getType(match));
						}
					}
				} else if (jsonElement.isJsonArray()) {
					for (JsonElement element : jsonElement.getAsJsonArray()) {
						if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
							String stringElem = element.getAsString();
							if (stringElem.charAt(0) == '!') {
								typesBlacklist.add(BiomeDictionary.Type.getType(stringElem.substring(1)));
							} else {
								typesWhitelist.add(BiomeDictionary.Type.getType(stringElem));
							}
						}
					}
				}

				// Accept if the biome has any types from the whitelist and no types from the blacklist.
				return b -> biomeHasTypes(b, typesWhitelist) && (typesBlacklist.size() == 0 || !biomeHasAnyType(b, typesBlacklist));
			}

			return b -> false;
		});

		event.register(SPECIES, new JsonBiomePropertyApplierSpecies());

		event.register(DENSITY, new JsonBiomePropertyApplierDensity());

		event.register(CHANCE, new JsonBiomePropertyApplierChance());

		event.register(CANCELVANILLA, (dbase, element, biome) -> {
			if (element.isJsonPrimitive()) {
				boolean cancel = element.getAsBoolean();
				//System.out.println("Biome " + (cancel ? "cancelled" : "uncancelled") + " for vanilla: " + biome);
				dbase.setCancelVanillaTreeGen(biome, cancel);
			}
		});

		event.register(MULTIPASS, (dbase, element, biome) -> {
			if (element.isJsonPrimitive()) {
				boolean multipass = element.getAsBoolean();

				if (multipass) {
					//System.out.println("Biome set for multipass: " + biome);

					//Enable poisson disc multipass of roofed forests to ensure maximum density even with large trees
					//by filling in gaps in the generation with smaller trees 
					dbase.setMultipass(biome, pass -> {
						switch (pass) {
							case 0:
								return 0;//Zero means to run as normal
							case 1:
								return 5;//Return only radius 5 on pass 1
							case 2:
								return 3;//Return only radius 3 on pass 2
							default:
								return -1;//A negative number means to terminate
						}
					});
				}
			}
		});

		event.register(SUBTERRANEAN, (dbase, element, biome) -> {
			if (element.isJsonPrimitive()) {
				boolean subterranean = element.getAsBoolean();
				//System.out.println("Biome set to subterranean: " + biome);
				dbase.setIsSubterranean(biome, subterranean);
			}
		});

		event.register(FORESTNESS, (dbase, element, biome) -> {
			if (element.isJsonPrimitive()) {
				float forestness = element.getAsFloat();
				//System.out.println("Forestness set for biome: " + biome + " at " + forestness);
				dbase.setForestness(biome, forestness);
			}
		});

		event.register(BLACKLIST, (dbase, element, biome) -> {
			if (element.isJsonPrimitive()) {
				boolean blacklist = element.getAsBoolean();
				if (blacklist) {
					//System.out.println("Blacklisted biome: " + biome);
					blacklistedBiomes.add(biome);
				} else {
					blacklistedBiomes.remove(biome);
				}
			}
		});

		event.register(RESET, (dbase, element, biome) -> {
			dbase.setCancelVanillaTreeGen(biome, false);
			dbase.setSpeciesSelector(biome, (pos, dirt, rnd) -> new SpeciesSelection(), Operation.REPLACE);
			dbase.setDensitySelector(biome, (rnd, nd) -> -1, Operation.REPLACE);
			dbase.setChanceSelector(biome, (rnd, spc, rad) -> EnumChance.UNHANDLED, Operation.REPLACE);
			dbase.setForestness(biome, 0.0f);
			dbase.setIsSubterranean(biome, false);
			dbase.setMultipass(biome, pass -> (pass == 0 ? 0 : -1));
		});

	}

	public BiomeDataBasePopulatorJson(ResourceLocation jsonLocation) {
		this(JsonHelper.load(jsonLocation));
	}

	public BiomeDataBasePopulatorJson(JsonElement jsonElement) {
		this.jsonElement = jsonElement;
	}

	@Override
	public void populate(BiomeDataBase biomeDataBase) {
		if (jsonElement != null && jsonElement.isJsonArray()) {
			for (JsonElement sectionElement : jsonElement.getAsJsonArray()) {
				if (sectionElement.isJsonObject()) {
					JsonObject section = sectionElement.getAsJsonObject();
					readSection(section, biomeDataBase);
				}
			}
		}
	}

	public static boolean biomeHasAnyType(Biome biome, List<BiomeDictionary.Type> types) {
		return types.stream().anyMatch(t -> BiomeDictionary.hasType(biome, t));
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

		for (Entry<String, JsonElement> entry : section.entrySet()) {

			String key = entry.getKey();
			JsonElement element = entry.getValue();

			if (!isComment(key)) {
				if (WHITE.equals(key)) {
					if (element.isJsonPrimitive()) {
						if ("all".equals(element.getAsString())) {
							blacklistedBiomes.clear();
						}
					}
				} else if (SELECT.equals(key)) {
					if (element.isJsonObject()) {
						for (Entry<String, JsonElement> selectElement : element.getAsJsonObject().entrySet()) {
							String selectorName = selectElement.getKey();
							if (!isComment(selectorName)) {
								IJsonBiomeSelector selector = jsonBiomeSelectorMap.get(selectorName);
								if (selector != null) {
									selectors.add(new JsonBiomeSelectorData(selector, selectElement.getValue()));
								} else {
									System.err.println("Json Error: Undefined selector property \"" + selectorName + "\"");
								}
							}
						}
					}
				} else if (APPLY.equals(key)) {
					if (element.isJsonObject()) {
						for (Entry<String, JsonElement> selectElement : element.getAsJsonObject().entrySet()) {
							String applierName = selectElement.getKey();
							if (!isComment(applierName)) {
								IJsonBiomeApplier applier = jsonBiomeApplierMap.get(applierName);
								if (applier != null) {
									appliers.add(new JsonBiomeApplierData(applier, selectElement.getValue()));
								} else {
									System.err.println("Json Error: Undefined applier property \"" + applierName + "\"");
								}
							}
						}
					}
				} else {
					System.err.println("Json Error: Undefined operation \"" + key + "\"");
				}
			}

		}

		//Filter biomes by selector predicates
		Stream<Biome> stream = Lists.newArrayList(Biome.REGISTRY).stream();
		for (JsonBiomeSelectorData s : selectors) {
			stream = stream.filter(s.getFilter());
		}

		//Filter blacklisted biomes
		stream = stream.filter(b -> !blacklistedBiomes.contains(b));

		//Apply all of the applicators to the database
		stream.forEach(biome -> {
			appliers.forEach(a -> a.apply(dbase, biome));
		});
	}

}
