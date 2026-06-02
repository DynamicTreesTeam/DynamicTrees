package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class FallingTreeEntityModelFabric extends FallingTreeEntityModel {

    public FallingTreeEntityModelFabric(FallingTreeEntity entity) {
        super(entity);
    }

    @Override
    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BranchDestructionData destructionData = entity.getDestroyData();
        Direction cutDir = destructionData.cutDir;

        ArrayList<TreeQuadData> treeQuads = new ArrayList<>();

        int[] connectionArray = new int[6];

        if (destructionData.getNumBranches() > 0) {
            BlockState exState = destructionData.getBranchBlockState(0);
            BlockPos cutPos = destructionData.cutPos;
            if (exState != null) {
                Species species = destructionData.species;
                RandomSource random = entity.getRandom();

                boolean rootyBlockAdded = false;
                if (destructionData.soilState != null) {
                    SoilBlock soilBlock = TreeHelper.getRooty(BuiltInRegistries.BLOCK.get(destructionData.soilState.getLeft()));
                    if (soilBlock != null) {
                        BlockState soilState = soilBlock.GetStateFromIndex(destructionData.soilState.getRight());
                        BakedModel rootyModel = dispatcher.getBlockModel(soilState);
                        BlockPos cutOffset = destructionData.getRelativeCutPos();
                        treeQuads.addAll(toTreeQuadData(
                                getQuadsWithOffset(rootyModel, soilState, new Vec3(cutOffset.getX(), cutOffset.getY() - 1, cutOffset.getZ()), random),
                                destructionData.species.getFamily().getRootColor(soilState, soilBlock.getColorFromBark()),
                                soilState));
                        rootyBlockAdded = true;
                    }
                }

                BakedModel branchModel = dispatcher.getBlockModel(exState);
                destructionData.getConnections(0, connectionArray);
                boolean bottomRingsAdded = false;
                if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0) {
                    BlockPos offsetPos = destructionData.getRelativeCutPos().relative(cutDir);
                    float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
                    int coreRadius = ((BranchBlock) exState.getBlock()).getRadius(exState);
                    treeQuads.addAll(toTreeQuadData(
                            getBottomRingQuads(branchModel, new Vec3(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), coreRadius, cutDir),
                            exState));
                    bottomRingsAdded = true;
                }

                for (int index = 0; index < destructionData.getNumBranches(); index++) {
                    Block previousBranch = exState.getBlock();
                    exState = destructionData.getBranchBlockState(index);
                    if (!previousBranch.equals(exState.getBlock())) {
                        branchModel = dispatcher.getBlockModel(exState);
                    }
                    BlockPos relPos = destructionData.getBranchRelPos(index);
                    destructionData.getConnections(index, connectionArray);
                    int coreRadius = ((BranchBlock) exState.getBlock()).getRadius(exState);
                    Direction forceRingDir = (index == 0 && bottomRingsAdded) ? cutDir : null;
                    treeQuads.addAll(toTreeQuadData(
                            getBranchQuadsWithConnections(branchModel, exState, new Vec3(relPos.getX(), relPos.getY(), relPos.getZ()), random, connectionArray, coreRadius, forceRingDir),
                            exState));
                }

                for (Pair<BlockPos, BlockState> leafLoc : destructionData.getAllLeavesWithPos()) {
                    BlockState leafState = leafLoc.getValue();
                    List<BakedQuad> bakedQuads = getQuadsWithOffset(dispatcher.getBlockModel(leafState), leafState,
                            new Vec3(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), random);

                    treeQuads.addAll(toTreeQuadData(bakedQuads, species.leafColorMultiplier(entity.level(),
                            cutPos.offset(leafLoc.getKey())), leafState));
                }
            }
        }

        return treeQuads;
    }

    private List<BakedQuad> getBottomRingQuads(BakedModel model, Vec3 offset, int coreRadius, Direction cutDir) {
        List<BakedQuad> allQuads = new ArrayList<>();

        if (model instanceof BasicBranchBlockBakedModel branchModel) {
            for (BakedQuad quad : branchModel.getRingQuads(coreRadius)) {
                if (quad.getDirection() == cutDir) {
                    allQuads.add(quad);
                }
            }
        }

        return offsetAllQuads(offset, allQuads);
    }

    private List<BakedQuad> getBranchQuadsWithConnections(BakedModel model, BlockState state, Vec3 offset, RandomSource random, int[] connections, int coreRadius, Direction forceRingDir) {
        List<BakedQuad> allQuads = new ArrayList<>();

        if (model instanceof BasicBranchBlockBakedModel branchModel) {
            int twigRadius = state.getBlock() instanceof BranchBlock branchBlock
                    ? branchBlock.getFamily().getPrimaryThickness()
                    : 1;

            branchModel.collectQuads(coreRadius, connections, twigRadius, forceRingDir)
                    .values().forEach(allQuads::addAll);
        } else {
            for (Direction direction : Direction.values()) {
                allQuads.addAll(model.getQuads(state, direction, random));
            }
            allQuads.addAll(model.getQuads(state, null, random));
        }

        return offsetAllQuads(offset, allQuads);
    }

    private List<BakedQuad> getQuadsWithOffset(BakedModel model, BlockState state, Vec3 offset, RandomSource random) {
        List<BakedQuad> allQuads = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            allQuads.addAll(model.getQuads(state, direction, random));
        }
        allQuads.addAll(model.getQuads(state, null, random));

        return offsetAllQuads(offset, allQuads);
    }

    private List<BakedQuad> offsetAllQuads(Vec3 offset, List<BakedQuad> allQuads) {
        if (offset.x() != 0 || offset.y() != 0 || offset.z() != 0) {
            List<BakedQuad> offsetQuads = new ArrayList<>();
            for (BakedQuad quad : allQuads) {
                offsetQuads.add(offsetQuad(quad, offset));
            }
            return offsetQuads;
        }

        return allQuads;
    }

    private BakedQuad offsetQuad(BakedQuad quad, Vec3 offset) {
        int[] vertexData = quad.getVertices().clone();

        for (int i = 0; i < 4; i++) {
            int baseIndex = i * 8;
            float x = Float.intBitsToFloat(vertexData[baseIndex]) + (float) offset.x();
            float y = Float.intBitsToFloat(vertexData[baseIndex + 1]) + (float) offset.y();
            float z = Float.intBitsToFloat(vertexData[baseIndex + 2]) + (float) offset.z();
            vertexData[baseIndex] = Float.floatToRawIntBits(x);
            vertexData[baseIndex + 1] = Float.floatToRawIntBits(y);
            vertexData[baseIndex + 2] = Float.floatToRawIntBits(z);
        }

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
}
