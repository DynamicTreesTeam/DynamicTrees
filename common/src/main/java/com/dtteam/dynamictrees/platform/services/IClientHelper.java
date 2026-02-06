package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;


public interface IClientHelper {

    int getPixelRGBA(TextureAtlasSprite sprite, int x, int y);


    FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity);

}
