package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Function;

public class ThickRingAtlasTextureManager {

    /**
     * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
     */

//    static AtlasTexture thickRingsAtlasTexture = new AtlasTexture(new ResourceLocation(DynamicTrees.MODID, "textures/atlas/thick_rings.png"));
//
    private static final Map<ResourceLocation, ResourceLocation> thickRingTextures = new HashMap<>();

	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
		ResourceLocation outputRes = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
		thickRingTextures.put(ringsRes, outputRes);
		return outputRes;
	}

	public static Collection<ResourceLocation> getThickRingResourceLocations (){
	    return thickRingTextures.values();
    }

//
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
