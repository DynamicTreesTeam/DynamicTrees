package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import java.io.File;

/**
 * A simple loader that reads a JSON file and facilitates database population
 * for multiple custom dimensions.
 * 
 * @author ferreusveritas
 *
 */
public class MultiDimensionalPopulator {
	
	public static final String DIM = "dim";
	public static final String ACTIVE = "active";
	public static final String FILES = "files";
	
	public MultiDimensionalPopulator(ResourceLocation jsonLocation, IBiomeDataBasePopulator defaultPopulator) {
		this(JsonHelper.load(jsonLocation), defaultPopulator);
	}
	
	public MultiDimensionalPopulator(JsonElement mainJsonElement, IBiomeDataBasePopulator defaultPopulator) {
		load(mainJsonElement, defaultPopulator);
	}
	
	private void load(JsonElement mainJsonElement, IBiomeDataBasePopulator defaultPopulator) {
		
		if(mainJsonElement != null && mainJsonElement.isJsonArray()) {
			for(JsonElement element : mainJsonElement.getAsJsonArray()) {
				if(element.isJsonObject()) {
					JsonObject obj = element.getAsJsonObject();
					
					//Let the json reader handle this and error out as necessary
					int dim = obj.get(DIM).getAsInt();
					boolean active = obj.get(ACTIVE).getAsBoolean();
					JsonArray files = obj.get(FILES).getAsJsonArray();
					
					if(active) {
						
						System.out.println("Loading custom populators for dimension: " + dim);
						
						//All new databases get a fresh coat of default population
						BiomeDataBase database = new BiomeDataBase();
						
						defaultPopulator.populate(database);
						
						//This creates a link for the dimension id to this new database
						//						TreeGenerator.getTreeGenerator().linkDimensionToDataBase(new Di, database);

						//Apply all of the referred json files
						for(JsonElement filename : files) {
							if(filename.isJsonPrimitive()) {
//								File file = new File(DTConfigs.configDirectory.getAbsolutePath() + "/" + filename.getAsString());
								//Each populator overwrites the results of the previous one in succession
//								new BiomeDataBasePopulatorJson(JsonHelper.load(file)).populate(database);
							}
						}
						
					}
				}
			}
		}
		
	}
	
}
