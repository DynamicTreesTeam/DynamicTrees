package com.dtteam.dynamictrees.model.entity;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.client.TintSourceHelper;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.DynamicModelRegistry;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.QuadManipulator;
import com.dtteam.dynamictrees.model.entity.render.FallingTreeRenderState;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FallingTreeEntityModel extends EntityModel<FallingTreeRenderState> {

    protected final List<TreeQuadData> quads;
    protected final Species species;

    public FallingTreeEntityModel(FallingTreeEntity entity) {
        super(new ModelPart(List.of(), Map.of()));
        BranchDestructionData destructionData = entity.getDestroyData();

        species = destructionData.species;
        quads = generateTreeQuads(entity);
    }

    public List<TreeQuadData> getQuads() {
        return quads;
    }

    public static int getBrightness(FallingTreeEntity entity) {
        final BranchDestructionData destructionData = entity.getDestroyData();
        final Level world = entity.level();
        return world.getBlockState(destructionData.cutPos).getLightEmission();
    }

    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        BlockStateModelSet modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        BranchDestructionData destructionData = entity.getDestroyData();
        Direction cutDir = destructionData.cutDir;

        ArrayList<TreeQuadData> treeQuads = new ArrayList<>();

        int[] connectionArray = new int[6];

        if (destructionData.getNumBranches() > 0) {
            BlockState exState = destructionData.getBranchBlockState(0);
            if (exState != null) {

                //Draw the rooty block if it is set to fall too
                boolean rootyBlockAdded = false;
                if (destructionData.soilState != null){
                    BlockState soilState = destructionData.soilState;
                    if (TreeHelper.isRooty(soilState)) {
                        SoilBlock soilBlock = TreeHelper.getRooty(soilState);
                        BlockStateModel rootyModel = DynamicModelRegistry.getOrFallback(soilState, modelSet);
                        BlockPos cutOffset = destructionData.getRelativeCutPos();
                        treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(rootyModel, soilState, new Vec3(cutOffset.getX(), cutOffset.getY()-1, cutOffset.getZ()), entity.getRandom(), null),
                                species.getFamily().getRootColor(soilState, soilBlock != null && soilBlock.getColorFromBark()),
                                soilState));
                        rootyBlockAdded = true;
                    }

                }

                BlockStateModel branchModel = DynamicModelRegistry.getOrFallback(exState, modelSet);
                //Draw the ring texture cap on the cut block if the rings connection is above 0
                destructionData.getConnections(0, connectionArray);
                boolean bottomRingsAdded = false;
                if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0) {
                    BlockPos offsetPos = destructionData.getRelativeCutPos().relative(cutDir);
                    float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), new Direction[]{null}, entity.getRandom(),
                                    new ModelConnections(cutDir).setFamily(TreeHelper.getBranch(exState))),
                            exState));
                    bottomRingsAdded = true;
                }

                //Draw the rest of the tree/branch
                for (int index = 0; index < destructionData.getNumBranches(); index++) {
                    Block previousBranch = exState == null ? Blocks.AIR : exState.getBlock();
                    exState = destructionData.getBranchBlockState(index);
                    if (exState == null) continue;
                    if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
                    {
                        branchModel = DynamicModelRegistry.getOrFallback(exState, modelSet);
                    }
                    BlockPos relPos = destructionData.getBranchRelPos(index);
                    destructionData.getConnections(index, connectionArray);
                    ModelConnections modelConnections = new ModelConnections(connectionArray).setFamily(TreeHelper.getBranch(exState));
                    if (index == 0 && bottomRingsAdded) {
                        modelConnections.setForceRing(cutDir);
                    }
                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(relPos.getX(), relPos.getY(), relPos.getZ()), entity.getRandom(), modelConnections),
                            exState));
                }

                //Draw the leaves
                for (Pair<BlockPos, BlockState> leafLoc : destructionData.getAllLeavesWithPos()) {
                    BlockState leafState = leafLoc.getValue();
                    List<BakedQuad> bakedQuads = QuadManipulator.getQuads(modelSet.get(leafState), leafState, new Vec3(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), entity.getRandom(), null);

                    ClientLevel level = Minecraft.getInstance().level;
                    int leavesColor = level == null ? 0xFFFFFF : TintSourceHelper.getLeavesColor(species, level, destructionData.basePos.offset(leafLoc.getKey()));
                    treeQuads.addAll(toTreeQuadData(bakedQuads, leavesColor, leafState));
                }
            }
        }

        return treeQuads;
    }

    @Override
    public void setupAnim(FallingTreeRenderState state) {
        super.setupAnim(state);
    }

    public void renderToBuffer(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay) {
        float r, g, b;
        for (TreeQuadData treeQuad : getQuads()) {
            r = 1;
            g = 1;
            b = 1;
            BakedQuad bakedQuad = treeQuad.bakedQuad;
            if (bakedQuad.materialInfo().isTinted()) {
                int color = (species == null) ? treeQuad.color : species.colorTreeQuads(treeQuad.color, treeQuad);
                r = (float) (color >> 16 & 255) / 255F;
                g = (float) (color >> 8 & 255) / 255F;
                b = (float) (color & 255) / 255F;
            }
            if (bakedQuad.materialInfo().shade()) {
                float diffuse = 0.8f;
                r *= diffuse;
                g *= diffuse;
                b *= diffuse;
            }
            int newColor = ((int)(r * 255F) & 255) << 16 | ((int)(g * 255F) & 255) << 8 | ((int)(b * 255F) & 255);
            QuadInstance instance = new QuadInstance();
            instance.setColor(newColor);
            instance.setLightCoords(packedLight);
            instance.setOverlayCoords(packedOverlay);
            buffer.putBakedQuad(pose, bakedQuad, instance);
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
