package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * @author Harley O'Connor
 */
public final class BakedModelEventHandler {

    //These locs are accessed by the model data generators
    public static final ResourceLocation BRANCH = DynamicTrees.location("branch");
    public static final ResourceLocation SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final ResourceLocation ROOTS = DynamicTrees.location("roots");

    @SubscribeEvent
    public void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        // Register model loaders for baked models.
//        event.register("branch", new BranchBlockModelLoader());
//        event.register("surface_root", new SurfaceRootBlockModelLoader());
//        event.register("thick_branch", new ThickBranchBlockModelLoader());
//        event.register("roots", new RootsBlockModelLoader());
//        event.register("large_palm_fronds", new PalmLeavesModelLoader(0));
//        event.register("medium_palm_fronds", new PalmLeavesModelLoader(1));
//        event.register("small_palm_fronds", new PalmLeavesModelLoader(2));
    }

    @SubscribeEvent
    public void onModelModifyBakingResultResult(ModelEvent.ModifyBakingResult event) {
        // Put bonsai pot baked model into its model location.
//        event.getModels().computeIfPresent(new ModelResourceLocation(PottedSaplingBlock.REG_NAME, ""), (k, val) -> new BakedModelBlockBonsaiPot(val));
    }

}