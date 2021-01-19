package com.ferreusveritas.dynamictrees.models.bakedmodels;

import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IDynamicBakedModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public abstract class BranchBlockBakedModel implements IDynamicBakedModel {

    public static List<BranchBlockBakedModel> INSTANCES = new ArrayList<>();

    protected final BlockModel blockModel;
    protected final ResourceLocation modelResLoc;
    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;

    public BranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        this.blockModel = new BlockModel(null, null, null, false, BlockModel.GuiLight.FRONT, ItemCameraTransforms.DEFAULT, null);

        this.modelResLoc = modelResLoc;
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;

        INSTANCES.add(this);
    }

    public abstract void setupModels ();

}
