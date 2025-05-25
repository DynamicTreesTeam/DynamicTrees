package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FallingTreeEntityModel extends EntityModel<FallingTreeEntity> {

    protected final List<TreeQuadData> quads;
    protected final int entityId;
    protected final Species species;

    public FallingTreeEntityModel(FallingTreeEntity entity) {
        BranchDestructionData destructionData = entity.getDestroyData();
        Species species = destructionData.species;

        quads = generateTreeQuads(entity);
        this.species = species;
        entityId = entity.getId();
    }

    public List<TreeQuadData> getQuads() {
        return quads;
    }

    public int getEntityId() {
        return entityId;
    }

    public static int getBrightness(FallingTreeEntity entity) {
        final BranchDestructionData destructionData = entity.getDestroyData();
        final Level world = entity.level();
        return world.getBlockState(destructionData.cutPos).getLightEmission();
    }

    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        return List.of();
    }

    @Override
    public void setupAnim(FallingTreeEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        float r, g, b;
        for (TreeQuadData treeQuad : getQuads()) {
            r = 1;
            g = 1;
            b = 1;
            BakedQuad bakedQuad = treeQuad.bakedQuad;
            if (bakedQuad.isTinted()) {
                color = (species == null) ? treeQuad.color : species.colorTreeQuads(treeQuad.color, treeQuad);
                r = (float) (color >> 16 & 255) / 255.0F;
                g = (float) (color >> 8 & 255) / 255.0F;
                b = (float) (color & 255) / 255.0F;
            }
            if (bakedQuad.isShade()) {
                float diffuse = 0.8f;
                r *= diffuse;
                g *= diffuse;
                b *= diffuse;
            }
            buffer.putBulkData(poseStack.last(), bakedQuad, r, g, b, 1, packedLight, packedOverlay);
        }
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, BlockState state) {
        return toTreeQuadData(bakedQuads, 0xFFFFFF, state);
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, int defaultColor, BlockState state) {
        return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, defaultColor, state)).collect(Collectors.toList());
    }

    public record TreeQuadData(BakedQuad bakedQuad, int color, BlockState state) { }
}
