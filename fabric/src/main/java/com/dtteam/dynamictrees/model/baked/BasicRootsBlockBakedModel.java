package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.model.BranchMultiPartHolder;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;

/**
 * Dynamic underground roots model for Fabric. Baked out of root-specific branch parts;
 * the part collection logic is shared with {@link BasicBranchBlockBakedModel}.
 * Mirrors the NeoForge {@code UnbakedRootsModel} baking.
 */
public class BasicRootsBlockBakedModel extends BasicBranchBlockBakedModel {

    public BasicRootsBlockBakedModel(BasicBranchBlockBakedModel baked) {
        super(baked.cores, baked.sleeves, baked.rings, baked.sleeveRings);
    }

    public static BasicRootsBlockBakedModel bakeRoots(ModelBaker baker, Material.Baked barkMat, Material.Baked ringsMat, boolean opaque) {
        BasicBranchBlockBakedModel model;
        if (opaque) {
            model = bakeBasic(baker,
                    new BranchModelPart.UnbakedCore(barkMat),
                    new BranchModelPart.UnbakedSleeve(barkMat),
                    new BranchModelPart.UnbakedCore(ringsMat),
                    new BranchModelPart.UnbakedRootSleeveEnds(ringsMat));
        } else {
            model = bakeBasic(baker,
                    new BranchModelPart.UnbakedRootCore(barkMat, true),
                    new BranchModelPart.UnbakedRootSleeve(barkMat),
                    new BranchModelPart.UnbakedRootCore(ringsMat, false),
                    null);
        }
        return new BasicRootsBlockBakedModel(model);
    }
}
