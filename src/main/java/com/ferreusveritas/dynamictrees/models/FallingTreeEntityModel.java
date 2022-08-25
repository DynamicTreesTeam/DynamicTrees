package com.ferreusveritas.dynamictrees.models;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FallingTreeEntityModel extends EntityModel<FallingTreeEntity> {

    protected final List<TreeQuadData> quads;
    //	protected Map<BakedQuad, Integer> quadTints;
    protected final int entityId;
    protected final Species species;

    public FallingTreeEntityModel(FallingTreeEntity entity) {
        Level world = entity.getCommandSenderWorld();
        BranchDestructionData destructionData = entity.getDestroyData();
        Species species = destructionData.species;

        quads = generateTreeQuads(entity);
//		quadTints = entity.getQuadTints();
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
        final Level world = entity.level;
        return world.getBlockState(destructionData.cutPos).getLightEmission(world, destructionData.cutPos);
    }

    public static List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BranchDestructionData destructionData = entity.getDestroyData();
        Direction cutDir = destructionData.cutDir;

        ArrayList<TreeQuadData> treeQuads = new ArrayList<>();

        int[] connectionArray = new int[6];

        if (destructionData.getNumBranches() > 0) {
            BlockState exState = destructionData.getBranchBlockState(0);
            BlockPos rootPos = destructionData.cutPos;
            if (exState != null) {
                Species species = destructionData.species;

                //Draw the rooty block if it is set to fall too
                BlockPos bottomPos = entity.blockPosition().below();
                BlockState bottomState = entity.level.getBlockState(bottomPos);
                boolean rootyBlockAdded = false;
                if (TreeHelper.isRooty(bottomState)) {
                    RootyBlock rootyBlock = TreeHelper.getRooty(bottomState);
                    if (rootyBlock != null && rootyBlock.fallWithTree(bottomState, entity.level, bottomPos)) {
                        BakedModel rootyModel = dispatcher.getBlockModel(bottomState);
                        treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(rootyModel, bottomState, new Vec3(0, -1, 0), ModelData.EMPTY),
                                destructionData.species.getFamily().getRootColor(bottomState, rootyBlock.getColorFromBark()),
                                bottomState));
                        rootyBlockAdded = true;
                    }
                }

                BakedModel branchModel = dispatcher.getBlockModel(exState);
                //Draw the ring texture cap on the cut block if the bottom connection is above 0
                destructionData.getConnections(0, connectionArray);
                boolean bottomRingsAdded = false;
                if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0) {
                    BlockPos offsetPos = BlockPos.ZERO.relative(cutDir);
                    float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), new Direction[]{null}, new ModelConnections(cutDir).setFamily(TreeHelper.getBranch(exState))),
                            exState));
                    bottomRingsAdded = true;
                }

                //Draw the rest of the tree/branch
                for (int index = 0; index < destructionData.getNumBranches(); index++) {
                    Block previousBranch = exState.getBlock();
                    exState = destructionData.getBranchBlockState(index);
                    if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
                    {
                        branchModel = dispatcher.getBlockModel(exState);
                    }
                    BlockPos relPos = destructionData.getBranchRelPos(index);
                    destructionData.getConnections(index, connectionArray);
                    ModelConnections modelConnections = new ModelConnections(connectionArray).setFamily(TreeHelper.getBranch(exState));
                    if (index == 0 && bottomRingsAdded) {
                        modelConnections.setForceRing(cutDir);
                    }
                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(relPos.getX(), relPos.getY(), relPos.getZ()), modelConnections),
                            exState));
                }

                //Draw the leaves
                final HashMap<BlockPos, BlockState> leavesClusters = species.getFellingLeavesClusters(destructionData);
                if (leavesClusters != null) {
                    for (Map.Entry<BlockPos, BlockState> leafLoc : leavesClusters.entrySet()) {
                        BlockState leafState = leafLoc.getValue();
                        treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(dispatcher.getBlockModel(leafState), leafState, new Vec3(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), ModelData.EMPTY),
                                species.leafColorMultiplier(entity.level, rootPos.offset(leafLoc.getKey())), leafState));
                    }
                } else {
                    for (int index = 0; index < destructionData.getNumLeaves(); index++) {
                        BlockPos relPos = destructionData.getLeavesRelPos(index);
                        BlockState leafState = destructionData.getLeavesBlockState(index);
                        BakedModel leavesModel = dispatcher.getBlockModel(leafState);
                        treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(leavesModel, leafState, new Vec3(relPos.getX(), relPos.getY(), relPos.getZ()), ModelData.EMPTY),
                                destructionData.getLeavesProperties(index).treeFallColorMultiplier(leafState, entity.level, rootPos.offset(relPos)), leafState));
                    }
                }

            }
        }

        return treeQuads;
    }

    @Override
    public void setupAnim(FallingTreeEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int color;
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
            buffer.putBulkData(matrixStack.last(), bakedQuad, r, g, b, packedLight, packedOverlay);
        }
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, BlockState state) {
        return toTreeQuadData(bakedQuads, 0xFFFFFF, state);
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, int defaultColor, BlockState state) {
        return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, defaultColor, state)).collect(Collectors.toList());
    }

    public static final class TreeQuadData {
        public final BakedQuad bakedQuad;
        public final BlockState state;
        public final int color;

        public TreeQuadData(BakedQuad bakedQuad, int color, BlockState state) {
            this.bakedQuad = bakedQuad;
            this.state = state;
            this.color = color;
        }
    }
}
