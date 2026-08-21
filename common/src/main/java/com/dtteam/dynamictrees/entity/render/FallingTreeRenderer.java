package com.dtteam.dynamictrees.entity.render;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.model.FallingTreeEntityModelTrackerCache;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity, FallingTreeRenderer.FallingTreeRenderState> {

    public FallingTreeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public FallingTreeRenderState createRenderState() {
        return new FallingTreeRenderState();
    }

    @Override
    public boolean shouldRender(FallingTreeEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return entity.isClientBuilt()
                && entity.shouldRender(entity.getX(), entity.getY(), entity.getX())
                && super.shouldRender(entity, camera, camX, camY, camZ);
    }

    @Override
    public void extractRenderState(FallingTreeEntity entity, FallingTreeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.visible = entity.isClientBuilt() && entity.shouldRender(entity.getX(), entity.getY(), entity.getX());
        state.model = null;
        if (!state.visible) {
            return;
        }
        state.model = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);
        state.animationName = entity.currentAnimationHandler.getName();
        state.yaw = Mth.wrapDegrees(MathUtils.angleDegreesInterpolate(entity.yRotO, entity.getYRot(), partialTick));
        state.pitch = Mth.wrapDegrees(MathUtils.angleDegreesInterpolate(entity.xRotO, entity.getXRot(), partialTick));
        state.massCenter = entity.getMassCenter();
        if (entity.getDestroyData().getNumBranches() > 0) {
            int radius = entity.getDestroyData().getBranchRadius(0);
            Direction toolDir = entity.getDestroyData().toolDir;
            state.toolVec = new Vec3(toolDir.getStepX(), toolDir.getStepY(), toolDir.getStepZ()).scale(radius / 16.0f);
        } else {
            state.toolVec = Vec3.ZERO;
        }
    }

    @Override
    public void submit(FallingTreeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible && state.model != null) {
            poseStack.pushPose();
            applyAnimationTransform(state, poseStack);
            FallingTreeEntityModel treeModel = state.model;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), (pose, consumer) -> {
                QuadInstance instance = new QuadInstance();
                instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
                instance.setLightCoords(state.lightCoords);
                Species species = treeModel.getSpecies();
                for (FallingTreeEntityModel.TreeQuadData treeQuad : treeModel.getQuads()) {
                    BakedQuad bakedQuad = treeQuad.bakedQuad();
                    int color = 0xFFFFFFFF;
                    if (bakedQuad.materialInfo().isTinted()) {
                        int rgb = species == null
                                ? treeQuad.color()
                                : species.colorTreeQuads(treeQuad.color(), treeQuad);
                        color = 0xFF000000 | (rgb & 0xFFFFFF);
                    }
                    instance.setColor(color);
                    if (bakedQuad.materialInfo().shade()) {
                        instance.scaleColor(0.8f);
                    }
                    consumer.putBakedQuad(pose, bakedQuad, instance);
                }
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    private static void applyAnimationTransform(FallingTreeRenderState state, PoseStack poseStack) {
        if ("fallover".equals(state.animationName)) {
            Vec3 toolVec = state.toolVec;
            poseStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
            poseStack.mulPose(Axis.ZN.rotationDegrees(state.yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
            poseStack.translate(toolVec.x, toolVec.y, toolVec.z);
            poseStack.translate(-0.5, 0, -0.5);
            return;
        }
        Vec3 massCenter = state.massCenter;
        poseStack.translate(massCenter.x, massCenter.y, massCenter.z);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
        poseStack.translate(-massCenter.x - 0.5, -massCenter.y, -massCenter.z - 0.5);
    }

    public static class FallingTreeRenderState extends EntityRenderState {
        public boolean visible;
        public FallingTreeEntityModel model;
        public String animationName = "void";
        public float yaw;
        public float pitch;
        public Vec3 massCenter = Vec3.ZERO;
        public Vec3 toolVec = Vec3.ZERO;
    }
}
