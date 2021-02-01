package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.JsonCapabilityRegistryEvent;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonHelper {
	
	public static JsonElement load(ResourceLocation jsonLocation) {
		String filename = "data/" + jsonLocation.getNamespace() + "/trees/" + jsonLocation.getPath();
		InputStream in = new LeavesPaging().getClass().getClassLoader().getResourceAsStream(filename);
		if(in == null) {
			DynamicTrees.getLogger().fatal("Could not open resource " + filename);
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return new Gson().fromJson(reader, JsonElement.class);
	}
	
	public static JsonElement load(File file) {
		
		if (file != null && file.exists() && file.isFile() && file.canRead()) {
			String fileName = file.getAbsolutePath();
			
			try {
				JsonParser parser = new JsonParser();
				return parser.parse(new FileReader(file));
			}
			catch (Exception e) {
				DynamicTrees.getLogger().fatal("Can't open " + fileName + ": " + e.getMessage());
			}
		}
		
		return null;
	}

}
