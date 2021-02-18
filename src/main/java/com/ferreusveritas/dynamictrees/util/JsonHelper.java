package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;

import java.io.*;

public class JsonHelper {

	public enum ResourceFolder {
		ASSETS("assets/"),
		DATA("data/"),
		TREES("trees/");

		private final String folderName;

		ResourceFolder(String folderName) {
			this.folderName = folderName;
		}
	}

	public static JsonElement load(ResourceLocation jsonLocation) {
		return load(jsonLocation, ResourceFolder.DATA);
	}

	public static JsonElement load(ResourceLocation jsonLocation, ResourceFolder resourceFolder) {
		String filename = resourceFolder.folderName + jsonLocation.getNamespace() + "/" + (resourceFolder == ResourceFolder.DATA ? "trees/" : "") + jsonLocation.getPath();
		DynamicTrees.getLogger().info(filename);
		InputStream in = LeavesPaging.class.getClassLoader().getResourceAsStream(filename);
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

	/**
	 * Gets the boolean value from the element name of the {@link JsonObject} given, or
	 * returns the default value given if the element was not found or wasn't a boolean.
	 *
	 * @param jsonObject The {@link JsonObject}.
	 * @param elementName The name of the element to get.
	 * @param defaultValue The default value if it couldn't be obtained.
	 * @return The boolean value.
	 */
	public static boolean getOrDefault (JsonObject jsonObject, String elementName, boolean defaultValue) {
		JsonElement element = jsonObject.get(elementName);

		if (element == null || !element.isJsonPrimitive() || !((JsonPrimitive) element).isBoolean())
			return defaultValue;

		return element.getAsBoolean();
	}

}
