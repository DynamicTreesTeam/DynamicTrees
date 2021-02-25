package com.ferreusveritas.dynamictrees.client.thickrings;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThickRingTextureManager {

    /**
     * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
     */

    public static ThickRingAtlasTexture textureAtlas;
	//public static ThickRingSpriteUploader uploader;
	public static AtlasTexture.SheetData thickRingData;

    private static final BiMap<ResourceLocation, ResourceLocation> thickRingTextures = HashBiMap.create();

	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
		ResourceLocation thickRingSet = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
		thickRingTextures.put(ringsRes, thickRingSet);
		//return thickRingSet;
		return ringsRes;
	}

	public static Set<ResourceLocation> getThickRingResourceLocations (){
	    return new HashSet<>(thickRingTextures.values());
    }
    public static Set<Map.Entry<ResourceLocation, ResourceLocation>> getThickRingEntrySet(){
		return thickRingTextures.entrySet();
	}

	public static ResourceLocation getThickRingFromBaseRing (ResourceLocation baseRing){
		return thickRingTextures.get(baseRing);
	}
	public static ResourceLocation getBaseRingFromThickRing (ResourceLocation thickRing){
		return thickRingTextures.inverse().get(thickRing);
	}

}
