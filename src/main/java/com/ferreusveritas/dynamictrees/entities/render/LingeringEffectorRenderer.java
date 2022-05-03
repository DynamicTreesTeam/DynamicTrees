package com.ferreusveritas.dynamictrees.entities.render;

import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

/**
 * @author Harley O'Connor
 */
public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity> {

    public LingeringEffectorRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(LingeringEffectorEntity entity) {
        return MissingTextureAtlasSprite.getLocation();
    }

    public static class Factory implements IRenderFactory<LingeringEffectorEntity> {

        @Override
        public EntityRenderer<LingeringEffectorEntity> createRenderFor(EntityRenderDispatcher manager) {
            return new LingeringEffectorRenderer(manager);
        }

    }

}
