package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.CactusBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.RootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.ThickBranchBlockModelLoader;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        // Register model loaders for baked models.
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "branch"), new BranchBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "cactus"), new CactusBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "root"), new RootBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MOD_ID, "thick_branch"), new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        //ThickRingAtlasTextureManager.uploadToAtlas();

        // Setup branch baked models (bakes cores and sleeves).
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);

        // Put bonsai pot baked model into its model location.
        IBakedModel flowerPotModel = event.getModelRegistry().get(new ModelResourceLocation(DTRegistries.bonsaiPotBlock.getRegistryName(), ""));
        event.getModelRegistry().put(new ModelResourceLocation(DTRegistries.bonsaiPotBlock.getRegistryName(), ""),
                new BakedModelBlockBonsaiPot(flowerPotModel));
    }

}
