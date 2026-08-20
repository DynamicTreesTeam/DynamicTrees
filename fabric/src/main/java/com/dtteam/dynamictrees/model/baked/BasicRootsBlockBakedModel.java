package com.dtteam.dynamictrees.model.baked;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;

public class BasicRootsBlockBakedModel extends BasicBranchBlockBakedModel {
    public BasicRootsBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture) {
        super(barkTexture, ringsTexture);
    }

    public BasicRootsBlockBakedModel(ModelBaker baker, Material.Baked bark, Material.Baked rings) {
        super(baker, bark, rings);
    }
}
