package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.client.ThickRingAtlasTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }

        for(Map.Entry<ResourceLocation, ResourceLocation> reslocs : ThickRingAtlasTextureManager.getThickRingResourceLocations()){
            ResourceLocation originalLogResLoc = reslocs.getKey();
            ResourceLocation thickLogResLoc = reslocs.getValue();
            SimpleReloadableResourceManager manager = (SimpleReloadableResourceManager)Minecraft.getInstance().getResourceManager();

            try {
                manager.getResource(thickLogResLoc);
            } catch (IOException e){
                ThickRingAtlasTextureManager.generateThickRingTexture(manager, originalLogResLoc, thickLogResLoc);
            }

            event.addSprite(thickLogResLoc);

        }
    }

}
