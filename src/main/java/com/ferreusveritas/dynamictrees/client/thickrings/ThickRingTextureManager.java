package com.ferreusveritas.dynamictrees.client.thickrings;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThickRingTextureManager {

    /**
     * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
     */

    public static final Map<ResourceLocation, ResourceLocation> thickRingTextures = new HashMap<>();

	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
		ResourceLocation thickRingSet = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
		thickRingTextures.put(thickRingSet, ringsRes);
		return thickRingSet;
	}

	public static Set<ResourceLocation> getThickRingResourceLocations (){
	    return thickRingTextures.keySet();
    }

//    public static void generateThickRingTexture (ResourceLocation originalResLoc, ResourceLocation thickResLoc){
//		if (spriteUploader == null){
//			DynamicTrees.getLogger().error("ThickRingSpriteUploader not added yet!");
//			return;
//		}
//		spriteUploader.getTextureAtlas().addThickRingSprite(originalResLoc, thickResLoc);
//	}

//	public static void uploadToAtlas (){
//        List<TextureAtlasSprite> sprites = new LinkedList<>();
//        for (Map.Entry<ResourceLocation, ResourceLocation> entry : thickRingTextures.entrySet()){
//            sprites.add(new ThickRingTextureAtlasSprite(thickRingsAtlasTexture, entry.getValue(), entry.getKey()));
//        }
//        thickRingsAtlasTexture.upload(new AtlasTexture.SheetData(new HashSet<>(thickRingTextures.values()), 48*sprites.size(), 48*sprites.size(), 0, sprites));
//    }
//
//    public static TextureAtlasSprite getTexture (ResourceLocation resourceLocation){
//        return thickRingsAtlasTexture.getSprite(resourceLocation);
//    }

//    @Override
//    protected IResourceManager prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
//
//        return null;
//    }
//
//    @Override
//    protected void apply(IResourceManager objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
//        thickRingsAtlasTexture.stitch(objectIn, thickRingTextures.values().stream(), )
//    }
}
