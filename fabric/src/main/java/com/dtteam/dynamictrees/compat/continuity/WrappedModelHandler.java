package com.dtteam.dynamictrees.compat.continuity;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BreakingOverlayModel;
import com.dtteam.dynamictrees.platform.Services;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.Nullable;

public abstract class WrappedModelHandler {

    private static WrappedModelHandler INSTANCE = null;

    public static WrappedModelHandler getInstance(){
        if (INSTANCE == null) {
            if (Services.PLATFORM.isModLoaded("continuity")){
                INSTANCE = new ContinuityWrappedModelHandler();
            } else {
                INSTANCE = new WrappedModelHandler() {};
            }
        }
        return INSTANCE;
    }

    @Nullable
    public BasicBranchBlockBakedModel unwrapBranchModel(BlockStateModel model){
        if (model instanceof BreakingOverlayModel overlay) {
            return unwrapBranchModel(overlay.inner());
        }
        if (model instanceof BasicBranchBlockBakedModel branchModel) {
            return branchModel;
        }
        return null;
    }

}
