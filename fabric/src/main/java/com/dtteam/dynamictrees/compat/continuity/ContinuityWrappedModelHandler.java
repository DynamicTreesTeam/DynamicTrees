package com.dtteam.dynamictrees.compat.continuity;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import me.pepperbell.continuity.client.model.CtmBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ContinuityWrappedModelHandler extends WrappedModelHandler {

    // Continuity's CtmBlockStateModel extends Fabric's WrapperBlockStateModel, whose
    // wrapped model field is protected with no public accessor.
    @Nullable
    private static final VarHandle WRAPPED_FIELD = findWrappedField();

    @Nullable
    private static VarHandle findWrappedField() {
        try {
            return MethodHandles.privateLookupIn(WrapperBlockStateModel.class, MethodHandles.lookup())
                    .findVarHandle(WrapperBlockStateModel.class, "wrapped", BlockStateModel.class);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Override
    public @Nullable BasicBranchBlockBakedModel unwrapBranchModel(BlockStateModel model) {
        if (model instanceof CtmBlockStateModel ctmModel && WRAPPED_FIELD != null) {
            return super.unwrapBranchModel((BlockStateModel) WRAPPED_FIELD.get(ctmModel));
        }
        return super.unwrapBranchModel(model);
    }
}
