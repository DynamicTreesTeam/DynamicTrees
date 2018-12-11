package com.ferreusveritas.dynamictrees.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.util.ResourceLocation;

public class JsonHelper {
	
	public static JsonElement load(ResourceLocation jsonLocation) {
		String filename = "assets/" + jsonLocation.getResourceDomain() + "/" + jsonLocation.getResourcePath();
		InputStream in = new LeavesPaging().getClass().getClassLoader().getResourceAsStream(filename);
		if(in == null) {
			Logger.getLogger(ModConstants.MODID).log(Level.SEVERE, "Could not open resource " + filename);
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
				System.err.println("Can't open " + fileName + ": " + e.getMessage());
			}
		}
		
		return null;
	}
	
}
