package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public static void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
        ResourceLocation eventAtlasLocation = event.getMap().location();
        if (eventAtlasLocation.equals(AtlasTexture.LOCATION_BLOCKS)) {
            SimpleReloadableResourceManager manager = (SimpleReloadableResourceManager)Minecraft.getInstance().getResourceManager();

            List<ResourceLocation> ringLocationsToGenerate = new LinkedList<>();
            boolean textureNotFound = true;
            for(Map.Entry<ResourceLocation, ResourceLocation> reslocs : ThickRingTextureManager.getThickRingEntrySet()){
                ResourceLocation thickLogResLoc = reslocs.getValue();

                try {
                    manager.getResource(new ResourceLocation(thickLogResLoc.getNamespace(), String.format("textures/%s%s", thickLogResLoc.getPath(), ".png")));
                    textureNotFound = false;
                } catch (IOException ignored){ }

                if (textureNotFound){
                    ringLocationsToGenerate.add(thickLogResLoc);
                }
            }

//            ThickRingTextureManager.textureAtlas = new ThickRingAtlasTexture();
//            ThickRingTextureManager.thickRingData = ThickRingTextureManager.textureAtlas.stitch(manager, ringLocationsToGenerate.stream(), EmptyProfiler.INSTANCE, 0);

        }
    }

    @SubscribeEvent
    public static void onTextureStitchEventPost(final TextureStitchEvent.Post event) {
//        if (event.getMap().getTextureLocation().equals(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE)) {
//            TextureManager textureManager = Minecraft.getInstance().textureManager;
//            ThickRingAtlasTexture atlastexture = ThickRingTextureManager.textureAtlas;
//            AtlasTexture.SheetData atlastexture$sheetdata = ThickRingTextureManager.thickRingData;
//
//            textureManager.mapTextureObjects.remove(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE);
//
//            atlastexture.upload(atlastexture$sheetdata);
//            textureManager.loadTexture(atlastexture.getTextureLocation(), atlastexture);
//            textureManager.bindTexture(atlastexture.getTextureLocation());
//            atlastexture.setBlurMipmap(atlastexture$sheetdata);
//        }

    }

}
