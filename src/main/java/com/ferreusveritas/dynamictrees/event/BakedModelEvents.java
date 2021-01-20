package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.CactusBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.RootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.ThickBranchBlockModelLoader;
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
@Mod.EventBusSubscriber(modid = DynamicTrees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEvents {

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MODID, "branch"), new BranchBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MODID, "cactus"), new CactusBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MODID, "root"), new RootBlockModelLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(DynamicTrees.MODID, "thick_branch"), new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);
    }

}
