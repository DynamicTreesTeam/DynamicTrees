package com.ferreusveritas.dynamictrees.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.client.ThickRingTextureAtlasSprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextureGenerationHandler {
	
	private static final Map<ResourceLocation, ResourceLocation> thickRingTextures = new HashMap<ResourceLocation, ResourceLocation>();
	
	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
		ResourceLocation thickRingsRes = new ResourceLocation(ringsRes.getResourceDomain(), ringsRes.getResourcePath() + "_thick");
		
		thickRingTextures.put(ringsRes, thickRingsRes);
		
		return thickRingsRes;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		
		for (Entry<ResourceLocation, ResourceLocation> entry : thickRingTextures.entrySet()) {
			ResourceLocation textureLocation = new ResourceLocation(entry.getValue().getResourceDomain(), String.format("%s/%s%s", event.getMap().getBasePath(), entry.getValue().getResourcePath(), ".png"));
			
			IResource resource;
			try {
				resource = resourceManager.getResource(textureLocation);
			} catch (IOException e) {
				resource = null;
			}
			
			if (resource != null) {
				event.getMap().registerSprite(entry.getValue());
			} else {
				event.getMap().setTextureEntry(new ThickRingTextureAtlasSprite(entry.getValue(), entry.getKey()));
			}
		}
	}

}
