package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.bakedmodels.CactusBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loaders.CactusBlockModelLoader;
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
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        BasicBranchBlockBakedModel.INSTANCES.forEach(BasicBranchBlockBakedModel::setupBakedModels);
        CactusBranchBlockBakedModel.INSTANCES.forEach(CactusBranchBlockBakedModel::setupBakedModels);
    }

}
