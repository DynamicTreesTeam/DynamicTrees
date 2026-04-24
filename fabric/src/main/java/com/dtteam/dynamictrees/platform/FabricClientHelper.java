package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.entity.FallingTreeEntityModel;
import com.dtteam.dynamictrees.model.FallingTreeEntityModelFabric;
import com.dtteam.dynamictrees.platform.services.IClientHelper;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FabricClientHelper implements IClientHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        try {
            SpriteContents contents = sprite.contents();
            NativeImage image = contents.originalImage;
            if (image != null) {
                return image.getPixelRGBA(x, y);
            }
            return 0;
        } catch (Exception e) {
            DynamicTrees.LOG.warn("Failed to get pixel from sprite: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModelFabric(entity);
    }

}
