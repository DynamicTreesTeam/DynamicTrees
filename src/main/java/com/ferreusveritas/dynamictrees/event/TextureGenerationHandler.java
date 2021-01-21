package com.ferreusveritas.dynamictrees.event;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.client.ThickRingTextureAtlasSprite;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = DynamicTrees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//public class TextureGenerationHandler {
//
//	private static final Map<ResourceLocation, ResourceLocation> thickRingTextures = new HashMap<>();
//
//	public static class DualResourceLocation extends ResourceLocation {
//		private ResourceLocation alternate;
//
//		public DualResourceLocation(ResourceLocation primary, ResourceLocation alternate) {
//			super(primary.getNamespace(), primary.getPath());
//			this.alternate = alternate;
//		}
//
//		public ResourceLocation getAlternate() {
//			return alternate;
//		}
//	}
//
//	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
//		ResourceLocation outputRes = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
//		thickRingTextures.put(ringsRes, outputRes);
//		return outputRes;
//	}
//
//	public static ResourceLocation addDualTextureLocations(ResourceLocation res1, ResourceLocation res2, ResourceLocation outputRes) {
//		thickRingTextures.put(new DualResourceLocation(res1, res2), outputRes);
//		return outputRes;
//	}
//
//	public static void stitchTextures(){
//
//	}
//
//	@SubscribeEvent
//	public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
//		if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
//			return;
//		}
//
//		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
//
//		for (Entry<ResourceLocation, ResourceLocation> entry : thickRingTextures.entrySet()) {
//
//			if(entry.getKey() instanceof DualResourceLocation) {
//				dualStitch(event, (DualResourceLocation) entry.getKey(), entry.getValue());
//				continue;
//			}
//
//			ResourceLocation textureLocation = new ResourceLocation(entry.getValue().getNamespace(), String.format("%s/%s%s", event.getMap().getTextureLocation().getPath(), entry.getValue().getPath(), ".png"));
//
//			IResource resource;
//			try {
//				resource = resourceManager.getResource(textureLocation);
//			} catch (IOException e) {
//				resource = null;
//			}
//
//			if (resource != null) {
//				event.addSprite(entry.getValue());
//			} else {
//				event.getMap().upload(new AtlasTexture.SheetData(Collections.singleton(entry.getValue()), 32, 32, 1, Collections.singletonList(new ThickRingTextureAtlasSprite(event.getMap(), entry.getValue(), entry.getKey()))));
//			}
//		}
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	public static void dualStitch(TextureStitchEvent.Pre event, DualResourceLocation key, ResourceLocation value) {
//		event.getMap().upload(new AtlasTexture.SheetData(Collections.singleton(value), 32, 32, 1, Collections.singletonList(new ThickRingTextureAtlasSprite(event.getMap(), value, key, key.getAlternate()))));
//	}
//
//
//}
