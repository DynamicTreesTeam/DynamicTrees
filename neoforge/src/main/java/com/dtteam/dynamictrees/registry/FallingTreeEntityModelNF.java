package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class FallingTreeEntityModelNF extends FallingTreeEntityModel {

    public FallingTreeEntityModelNF(FallingTreeEntity entity) {
        super(entity);
    }

//    public FallingTreeEntityModelNF(FallingTreeEntity entity) {
//        super(entity);
//    }
//
//    @Override
//    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
//        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
//        BranchDestructionData destructionData = entity.getDestroyData();
//        Direction cutDir = destructionData.cutDir;
//
//        ArrayList<TreeQuadData> treeQuads = new ArrayList<>();
//
//        int[] connectionArray = new int[6];
//
//        if (destructionData.getNumBranches() > 0) {
//            BlockState exState = destructionData.getBranchBlockState(0);
//            BlockPos cutPos = destructionData.cutPos;
//            if (exState != null) {
//                Species species = destructionData.species;
//
//                //Draw the rooty block if it is set to fall too
//                boolean rootyBlockAdded = false;
//                if (destructionData.soilState != null){
//                    SoilBlock soilBlock = TreeHelper.getRooty(BuiltInRegistries.BLOCK.get(destructionData.soilState.getLeft()));
//                    if (soilBlock != null) {
//                        BlockState soilState = soilBlock.GetStateFromIndex(destructionData.soilState.getRight());
//                        BakedModel rootyModel = dispatcher.getBlockModel(soilState);
//                        BlockPos cutOffset = destructionData.getRelativeCutPos();
//                        treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(rootyModel, soilState, new Vec3(cutOffset.getX(), cutOffset.getY()-1, cutOffset.getZ()), entity.getRandom(), ModelData.EMPTY),
//                                destructionData.species.getFamily().getRootColor(soilState, soilBlock.getColorFromBark()),
//                                soilState));
//                        rootyBlockAdded = true;
//                    }
//
//                }
//
//                BakedModel branchModel = dispatcher.getBlockModel(exState);
//                //Draw the ring texture cap on the cut block if the rings connection is above 0
//                destructionData.getConnections(0, connectionArray);
//                boolean bottomRingsAdded = false;
//                if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0) {
//                    BlockPos offsetPos = destructionData.getRelativeCutPos().relative(cutDir);
//                    float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
//                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), new Direction[]{null}, entity.getRandom(),
//                                    new ModelConnections(cutDir).setFamily(TreeHelper.getBranch(exState)).toModelData()),
//                            exState));
//                    bottomRingsAdded = true;
//                }
//
//                //Draw the rest of the tree/branch
//                for (int index = 0; index < destructionData.getNumBranches(); index++) {
//                    Block previousBranch = exState.getBlock();
//                    exState = destructionData.getBranchBlockState(index);
//                    if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
//                    {
//                        branchModel = dispatcher.getBlockModel(exState);
//                    }
//                    BlockPos relPos = destructionData.getBranchRelPos(index);
//                    destructionData.getConnections(index, connectionArray);
//                    ModelConnections modelConnections = new ModelConnections(connectionArray).setFamily(TreeHelper.getBranch(exState));
//                    if (index == 0 && bottomRingsAdded) {
//                        modelConnections.setForceRing(cutDir);
//                    }
//                    treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3(relPos.getX(), relPos.getY(), relPos.getZ()), entity.getRandom(), modelConnections.toModelData()),
//                            exState));
//                }
//
//                //Draw the leaves
//                for (Pair<BlockPos, BlockState> leafLoc : destructionData.getAllLeavesWithPos()) {
//                    BlockState leafState = leafLoc.getValue();
//                    List<BakedQuad> bakedQuads = QuadManipulator.getQuads(dispatcher.getBlockModel(leafState), leafState, new Vec3(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), entity.getRandom(), ModelData.EMPTY);
//
//                    treeQuads.addAll(toTreeQuadData(bakedQuads, species.leafColorMultiplier(entity.level(),
//                            cutPos.offset(leafLoc.getKey())), leafState));
//                }
//            }
//        }
//
//        return treeQuads;
//    }
}
