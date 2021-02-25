package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingAtlasTexture;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.RootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.ThickBranchBlockModelLoader;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        // Register model loaders for baked models.
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "branch"), new BranchBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "root"), new RootBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "thick_branch"), new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // Setup branch baked models (bakes cores and sleeves).
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);

        // Put bonsai pot baked model into its model location.
        IBakedModel flowerPotModel = event.getModelRegistry().get(new ModelResourceLocation(DTRegistries.bonsaiPotBlock.getRegistryName(), ""));
        event.getModelRegistry().put(new ModelResourceLocation(DTRegistries.bonsaiPotBlock.getRegistryName(), ""),
                new BakedModelBlockBonsaiPot(flowerPotModel));

        ////Highly experimental code

        SpriteMap spriteAtlases = event.getModelManager().atlases;
        assert spriteAtlases != null;
        Map<ResourceLocation, AtlasTexture> atlasTextures = spriteAtlases.atlasTextures;
        atlasTextures.put(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE, ThickRingTextureManager.textureAtlas);

        Map<ResourceLocation, IUnbakedModel> topUnbakedModels = event.getModelLoader().topUnbakedModels;
        List<ResourceLocation> modelsToRebake = new LinkedList<>();
        for (Map.Entry<ResourceLocation, IUnbakedModel> entry : topUnbakedModels.entrySet()){
            for (RenderMaterial material : entry.getValue().getTextures(event.getModelLoader()::getUnbakedModel, Sets.newLinkedHashSet())){
                if (material.getAtlasLocation().equals(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE)){
                    modelsToRebake.add(entry.getKey());
                }
            }
        }
        for (ResourceLocation resourceLocation : modelsToRebake){
            event.getModelRegistry().put(resourceLocation, event.getModelLoader().bake(resourceLocation, ModelRotation.X0_Y0));
        }
    }

}
