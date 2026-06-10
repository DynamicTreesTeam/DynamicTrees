package com.dtteam.dynamictrees.compat.continuity;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import me.pepperbell.continuity.client.model.CtmBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import org.jetbrains.annotations.Nullable;

public class ContinuityWrappedModelHandler extends WrappedModelHandler {

    @Override
    public @Nullable BasicBranchBlockBakedModel unwrapBranchModel(BakedModel model) {
        if (model instanceof CtmBakedModel ctmModel){
            return super.unwrapBranchModel(ctmModel.getWrappedModel());
        }
        return super.unwrapBranchModel(model);
    }
}
