package com.dtteam.dynamictrees.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

/**
 * Wraps the base flower pot model for the potted sapling block. Currently a plain passthrough
 * (matching the pre-port Fabric behavior, where only the pot geometry came from the model).
 */
public class BakedModelBlockPottedSapling extends WrapperBlockStateModel {

    public BakedModelBlockPottedSapling(BlockStateModel basePotModel) {
        super(basePotModel);
    }
}
