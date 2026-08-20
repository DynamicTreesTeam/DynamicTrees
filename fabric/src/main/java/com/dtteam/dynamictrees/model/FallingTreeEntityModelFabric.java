package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;

import java.util.ArrayList;
import java.util.List;

public class FallingTreeEntityModelFabric extends FallingTreeEntityModel {

    public FallingTreeEntityModelFabric(FallingTreeEntity entity) {
        super(entity);
    }

    @Override
    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        return new ArrayList<>();
    }
}
