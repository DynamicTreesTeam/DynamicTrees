package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.event.handlers.BakedModelEventHandler;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Holds general model data and sets up a {@link BlockModel} for branch block baked models.
 *
 * <p>Main implementation is {@link BasicBranchBlockBakedModel}, which is the baked model
 * for dynamic branches with radius 1-8.</p>
 *
 * @author Harley O'Connor
 */
public abstract class BranchBlockBakedModel implements IDynamicBakedModel {

    /** A list of {@link BranchBlockBakedModel} instances, so that {@link #setupModels} can be
     * called in {@link BakedModelEventHandler}. */
    public static List<BranchBlockBakedModel> INSTANCES = new ArrayList<>();

    protected final BlockModel blockModel;

    protected final ResourceLocation modelResLoc;
    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;

    public BranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemCameraTransforms.NO_TRANSFORMS, ItemOverrideList.EMPTY.getOverrides());

        this.modelResLoc = modelResLoc;
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;

        INSTANCES.add(this);
    }

    /**
     * Sets up the {@link IBakedModel} objects for the model. This is called from
     * {@link BakedModelEventHandler#onModelBake(ModelBakeEvent)}, once the textures have
     * been stitched and so can be baked onto models.
     */
    public abstract void setupModels ();

}
