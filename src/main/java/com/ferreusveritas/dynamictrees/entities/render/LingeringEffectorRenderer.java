package com.ferreusveritas.dynamictrees.entities.render;

import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

/**
 * @author Harley O'Connor
 */
public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity> {

    public LingeringEffectorRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(LingeringEffectorEntity entity) {
        return MissingTextureSprite.getLocation();
    }

    public static class Factory implements IRenderFactory<LingeringEffectorEntity> {

        @Override
        public EntityRenderer<LingeringEffectorEntity> createRenderFor(EntityRendererManager manager) {
            return new LingeringEffectorRenderer(manager);
        }

    }

}
