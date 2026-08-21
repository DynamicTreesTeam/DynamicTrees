package com.dtteam.dynamictrees.entity.render;

import com.dtteam.dynamictrees.entity.LingeringEffectorEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Effector visuals are particles from {@code SubstanceEffect.update}, not a mesh.
 */
public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity, EntityRenderState> {

    public LingeringEffectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
