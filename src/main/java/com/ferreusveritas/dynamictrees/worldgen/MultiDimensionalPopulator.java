package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

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
		
		JsonElement mainJsonElement = null;
		
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(jsonLocation).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			mainJsonElement = new Gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
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
						TreeGenerator.getTreeGenerator().linkDimensionToDataBase(dim, database);
						
						//Apply all of the referred json files
						for(JsonElement filename : files) {
							//Each populator overwrites the results of the previous one in succession
							new BiomeDataBasePopulatorJson(new ResourceLocation(ModConstants.MODID, filename.getAsString())).populate(database);
						}
						
					}
				}
			}
		}
		
	}
	
}
