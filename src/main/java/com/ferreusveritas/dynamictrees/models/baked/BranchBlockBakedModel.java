package com.ferreusveritas.dynamictrees.models.baked;

import com.ferreusveritas.dynamictrees.event.handler.BakedModelEventHandler;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IDynamicBakedModel;

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

    /**
     * A list of {@link BranchBlockBakedModel} instances, so that {@link #setupModels} can be called in {@link
     * BakedModelEventHandler}.
     */
    public static final List<BranchBlockBakedModel> INSTANCES = new ArrayList<>();

    protected final BlockModel blockModel;

    protected final ResourceLocation modelResLoc;
    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;

    public BranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());

        this.modelResLoc = modelResLoc;
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;

        INSTANCES.add(this);
    }

    /**
     * BakedModelEventHandler#onModelBake(ModelBakeEvent)}, once the textures have been stitched and so can be baked
     * onto models.
     */
    public abstract void setupModels();

}