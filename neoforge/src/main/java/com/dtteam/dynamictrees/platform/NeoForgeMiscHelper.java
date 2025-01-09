package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.registry.FallingTreeEntityModelNF;
import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.Level;

public class NeoForgeMiscHelper implements IMiscHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        return sprite.getPixelRGBA(0, x, y);
    }

    @Override
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModelNF(entity);
    }

    @Override
    public boolean isLevelRestoringBlockSnapshots(Level level) {
        return level.restoringBlockSnapshots;
    }

}