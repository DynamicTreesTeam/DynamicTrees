package com.ferreusveritas.dynamictrees.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class JsonHelper {
	
	public static JsonElement load(ResourceLocation jsonLocation) {
		
		JsonElement mainJsonElement = null;
		
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(jsonLocation).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			mainJsonElement = new Gson().fromJson(reader, JsonElement.class);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return mainJsonElement;
	}
	
	public static JsonElement load(File file) {
		
		if (file != null && file.exists() && file.isFile() && file.canRead()) {
			String fileName = file.getAbsolutePath();
			
			try {
				JsonParser parser = new JsonParser();
				return parser.parse(new FileReader(file));
			}
			catch (Exception e) {
				System.err.println("Can't open " + fileName + ": " + e.getMessage());
			}
		}
		
		return null;
	}
	
}
