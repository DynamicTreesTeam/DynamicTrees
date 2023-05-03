package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.models.baked.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.baked.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loader.RootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loader.ThickBranchBlockModelLoader;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    public static final ResourceLocation BRANCH = DynamicTrees.location("branch");
    public static final ResourceLocation ROOT = DynamicTrees.location("root");
    public static final ResourceLocation THICK_BRANCH = DynamicTrees.location("thick_branch");

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterGeometryLoaders event) {
        // Register model loaders for baked models.
        event.register("branch", new BranchBlockModelLoader());
        event.register("root", new RootBlockModelLoader());
        event.register("thick_branch", new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(BakingCompleted event) {
        // Setup branch baked models (bakes cores and sleeves).
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);
        BranchBlockBakedModel.INSTANCES.clear();

        // Put bonsai pot baked model into its model location.
        BakedModel flowerPotModel = event.getModelManager().getModel(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""));
        event.getModels().put(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""),
                new BakedModelBlockBonsaiPot(flowerPotModel));

        ////Highly experimental code
//        SpriteMap spriteAtlases = event.getModelManager().atlases;
//        assert spriteAtlases != null;
//        Map<ResourceLocation, AtlasTexture> atlasTextures = spriteAtlases.atlasTextures;
//        atlasTextures.put(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE, ThickRingTextureManager.textureAtlas);
//
//        Map<ResourceLocation, IUnbakedModel> topUnbakedModels = event.getModelLoader().topUnbakedModels;
//        List<ResourceLocation> modelsToRebake = new LinkedList<>();
//        for (Map.Entry<ResourceLocation, IUnbakedModel> entry : topUnbakedModels.entrySet()){
//            for (RenderMaterial material : entry.getValue().getTextures(event.getModelLoader()::getUnbakedModel, Sets.newLinkedHashSet())){
//                if (material.getAtlasLocation().equals(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE)){
//                    modelsToRebake.add(entry.getKey());
//                }
//            }
//        }
//        for (ResourceLocation resourceLocation : modelsToRebake){
//            event.getModelRegistry().put(resourceLocation, event.getModelLoader().bake(resourceLocation, ModelRotation.X0_Y0));
//        }
    }

}