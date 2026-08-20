package com.dtteam.dynamictrees.compat.continuity;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class ContinuityWrappedModelHandler extends WrappedModelHandler {

    private static final Field WRAPPED_FIELD;

    static {
        Field wrapped = null;
        try {
            wrapped = WrapperBlockStateModel.class.getDeclaredField("wrapped");
            wrapped.setAccessible(true);
        } catch (ReflectiveOperationException ignored) {
        }
        WRAPPED_FIELD = wrapped;
    }

    @Override
    public @Nullable BasicBranchBlockBakedModel unwrapBranchModel(BlockStateModel model) {
        BlockStateModel current = model;
        for (int i = 0; i < 8 && current instanceof WrapperBlockStateModel && WRAPPED_FIELD != null; i++) {
            try {
                Object inner = WRAPPED_FIELD.get(current);
                if (!(inner instanceof BlockStateModel next) || next == current) {
                    break;
                }
                current = next;
            } catch (IllegalAccessException e) {
                break;
            }
        }
        return super.unwrapBranchModel(current);
    }
}
