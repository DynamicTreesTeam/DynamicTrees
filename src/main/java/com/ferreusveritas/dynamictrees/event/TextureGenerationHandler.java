package com.ferreusveritas.dynamictrees.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.client.ThickRingTextureAtlasSprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
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
		for (Entry<ResourceLocation, ResourceLocation> entry : thickRingTextures.entrySet()) {
			event.getMap().setTextureEntry(new ThickRingTextureAtlasSprite(entry.getValue(), entry.getKey()));
		}
	}

}
