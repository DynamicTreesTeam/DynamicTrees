package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.ITextureHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class NeoForgeTextureHelper implements ITextureHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        return sprite.getPixelRGBA(0, x, y);
    }
}