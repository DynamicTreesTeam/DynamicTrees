package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.entity.FallingTreeEntityModel;

/**
 * Fabric falling tree entity model. Since the 26.2 port, quad generation is fully handled by the
 * common {@link FallingTreeEntityModel} (via {@code QuadManipulator} and the
 * {@code BlockStateModelWithConnectionData} interface implemented by DT's Fabric block state models),
 * so no Fabric-specific behavior remains.
 */
public class FallingTreeEntityModelFabric extends FallingTreeEntityModel {

    public FallingTreeEntityModelFabric(FallingTreeEntity entity) {
        super(entity);
    }
}
