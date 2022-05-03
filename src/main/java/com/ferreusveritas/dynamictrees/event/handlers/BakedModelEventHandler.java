package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.RootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.ThickBranchBlockModelLoader;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
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

    public static final ResourceLocation BRANCH = DynamicTrees.resLoc("branch");
    public static final ResourceLocation ROOT = DynamicTrees.resLoc("root");
    public static final ResourceLocation THICK_BRANCH = DynamicTrees.resLoc("thick_branch");

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        // Register model loaders for baked models.
        ModelLoaderRegistry.registerLoader(BRANCH, new BranchBlockModelLoader());
        ModelLoaderRegistry.registerLoader(ROOT, new RootBlockModelLoader());
        ModelLoaderRegistry.registerLoader(THICK_BRANCH, new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // Setup branch baked models (bakes cores and sleeves).
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);
        BranchBlockBakedModel.INSTANCES.clear();

        // Put bonsai pot baked model into its model location.
        BakedModel flowerPotModel = event.getModelRegistry().get(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""));
        event.getModelRegistry().put(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""),
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
