package com.ferreusveritas.dynamictrees.event;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
		System.out.println("\n\n\n\n");
		for (ResourceLocation resloc : thickRingTextures.keySet()) {
			System.out.println(resloc);
		}
		System.out.println("\n\n\n\n");
		// TODO: add generated textures to texture atlas
	}

}
