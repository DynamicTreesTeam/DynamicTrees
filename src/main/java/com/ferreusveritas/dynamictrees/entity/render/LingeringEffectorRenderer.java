package com.ferreusveritas.dynamictrees.entity.render;

import com.ferreusveritas.dynamictrees.entity.LingeringEffectorEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity> {

    public LingeringEffectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(LingeringEffectorEntity entity) {
        return MissingTextureAtlasSprite.getLocation();
    }

}
