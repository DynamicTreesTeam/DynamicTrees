package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingAtlasTexture;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
//        if (event.getMap() instanceof ThickRingAtlasTexture) {
//            for(Map.Entry<ResourceLocation, ResourceLocation> reslocs : ThickRingTextureManager.getThickRingResourceLocations()){
//                ResourceLocation thickLogResLoc = reslocs.getKey();
//
//                event.addSprite(thickLogResLoc);
//            }
//        }
    }
}
