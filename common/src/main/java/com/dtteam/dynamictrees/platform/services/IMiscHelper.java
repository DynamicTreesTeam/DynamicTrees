package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.Level;

public interface IMiscHelper {

    int getPixelRGBA (TextureAtlasSprite sprite, int x, int y);

    FallingTreeEntityModel newFallingTreeEntityModel (FallingTreeEntity entity);

    boolean isLevelRestoringBlockSnapshots (Level level);

}
