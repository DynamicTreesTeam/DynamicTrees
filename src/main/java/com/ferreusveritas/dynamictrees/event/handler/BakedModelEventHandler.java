package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.models.baked.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loader.RootsBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loader.SurfaceRootBlockModelLoader;
import com.ferreusveritas.dynamictrees.models.loader.ThickBranchBlockModelLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    public static final ResourceLocation BRANCH = DynamicTrees.location("branch");
    public static final ResourceLocation SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final ResourceLocation ROOTS = DynamicTrees.location("roots");
    public static final ResourceLocation THICK_BRANCH = DynamicTrees.location("thick_branch");

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterGeometryLoaders event) {
        // Register model loaders for baked models.
        event.register("branch", new BranchBlockModelLoader());
        event.register("surface_root", new SurfaceRootBlockModelLoader());
        event.register("thick_branch", new ThickBranchBlockModelLoader());
        event.register("roots", new RootsBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelModifyBakingResultResult(ModelEvent.ModifyBakingResult event) {
        // Put bonsai pot baked model into its model location.
        event.getModels().computeIfPresent(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""), (k, val) -> new BakedModelBlockBonsaiPot(val));
    }
}