package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.compat.continuity.WrappedModelHandler;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FallingTreeEntityModelFabric extends FallingTreeEntityModel {

    public FallingTreeEntityModelFabric(FallingTreeEntity entity) {
        super(entity);
    }

    @Override
    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        BranchDestructionData destructionData = entity.getDestroyData();
        Direction cutDir = destructionData.cutDir;
        ArrayList<TreeQuadData> treeQuads = new ArrayList<>();
        int[] connectionArray = new int[6];
        RandomSource random = entity.getRandom();

        if (destructionData.getNumBranches() <= 0) {
            return treeQuads;
        }

        BlockState exState = destructionData.getBranchBlockState(0);
        BlockPos cutPos = destructionData.cutPos;
        if (exState == null) {
            return treeQuads;
        }

        Species species = destructionData.species;
        BlockStateModelSet models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

        boolean rootyBlockAdded = false;
        if (destructionData.soilState != null) {
            SoilBlock soilBlock = TreeHelper.getRooty(BuiltInRegistries.BLOCK.getValue(destructionData.soilState.getLeft()));
            if (soilBlock != null) {
                BlockState soilState = soilBlock.GetStateFromIndex(destructionData.soilState.getRight());
                BlockPos cutOffset = destructionData.getRelativeCutPos();
                Vec3 offset = new Vec3(cutOffset.getX(), cutOffset.getY() - 1, cutOffset.getZ());
                treeQuads.addAll(toTreeQuadData(
                        BakedQuadMover.move(collectVanillaQuads(models.get(soilState), random), offset),
                        destructionData.species.getFamily().getRootColor(soilState, soilBlock.getColorFromBark()),
                        soilState));
                rootyBlockAdded = true;
            }
        }

        destructionData.getConnections(0, connectionArray);
        boolean bottomRingsAdded = false;
        if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0) {
            BlockPos offsetPos = destructionData.getRelativeCutPos().relative(cutDir);
            float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
            Vec3 capOffset = new Vec3(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset);
            treeQuads.addAll(toTreeQuadData(
                    BakedQuadMover.move(collectBranchQuads(models.get(exState), exState, new int[6], cutDir, random), capOffset),
                    exState));
            bottomRingsAdded = true;
        }

        for (int index = 0; index < destructionData.getNumBranches(); index++) {
            BlockState nextState = destructionData.getBranchBlockState(index);
            if (nextState == null) {
                continue;
            }
            exState = nextState;
            BlockPos relPos = destructionData.getBranchRelPos(index);
            destructionData.getConnections(index, connectionArray);
            Direction forceRing = (index == 0 && bottomRingsAdded) ? cutDir : null;
            treeQuads.addAll(toTreeQuadData(
                    BakedQuadMover.move(collectBranchQuads(models.get(exState), exState, connectionArray, forceRing, random),
                            new Vec3(relPos.getX(), relPos.getY(), relPos.getZ())),
                    exState));
        }

        for (Pair<BlockPos, BlockState> leafLoc : destructionData.getAllLeavesWithPos()) {
            BlockState leafState = leafLoc.getValue();
            BlockPos leafPos = leafLoc.getKey();
            List<BakedQuad> bakedQuads = BakedQuadMover.move(
                    collectVanillaQuads(models.get(leafState), random),
                    new Vec3(leafPos.getX(), leafPos.getY(), leafPos.getZ()));
            treeQuads.addAll(toTreeQuadData(bakedQuads, species.leafColorMultiplier(entity.level(), cutPos.offset(leafPos)), leafState));
        }

        return treeQuads;
    }

    private static List<BakedQuad> collectBranchQuads(BlockStateModel model, BlockState state, int[] connections,
                                                      @Nullable Direction forceRingDir, RandomSource random) {
        BasicBranchBlockBakedModel branchModel = WrappedModelHandler.getInstance().unwrapBranchModel(model);
        if (branchModel != null) {
            return branchModel.collectQuads(state, connections, forceRingDir);
        }
        return collectVanillaQuads(model, random);
    }

    private static List<BakedQuad> collectVanillaQuads(BlockStateModel model, RandomSource random) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            if (part instanceof SimpleModelWrapper wrapper) {
                quads.addAll(wrapper.quads().getAll());
            } else {
                for (Direction face : Direction.values()) {
                    quads.addAll(part.getQuads(face));
                }
            }
        }
        return quads;
    }
}
