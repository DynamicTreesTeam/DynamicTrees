package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.platform.services.IClientHelper;
import com.dtteam.dynamictrees.registry.FallingTreeEntityModelNF;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class NeoForgeClientHelper implements IClientHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        try {
            return sprite.getPixelRGBA(0, x, y);
        } catch (IllegalStateException e) {
            DynamicTrees.LOG.warn("Image {} is not allocated.", sprite);
            return 0;
        }
    }

    @Override
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModelNF(entity);
    }

}
