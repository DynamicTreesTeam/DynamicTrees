package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class InflatorNode implements NodeInspector {

    private float radius;
    private BlockPos last;
    private BlockPos highestTrunkBlock;
    final private int maxRadius;
    final private int primaryThickness;
    final private int secondaryThickness;
    final private float tapering;

    Species species;
    SimpleVoxmap leafMap;

    public InflatorNode(Species species, SimpleVoxmap leafMap, int maxRadius) {
        this(species, leafMap, maxRadius,
                species.getFamily().getPrimaryThickness(), species.getFamily().getSecondaryThickness(),
                species.getTapering() * species.getWorldGenTaperingFactor());
    }
    public InflatorNode(Species species, SimpleVoxmap leafMap, int maxRadius, int primaryThickness, int secondaryThickness, float tapering) {
        this.species = species;
        this.primaryThickness = primaryThickness;
        this.secondaryThickness = secondaryThickness;
        this.tapering = tapering;
        this.leafMap = leafMap;
        last = BlockPos.ZERO;
        highestTrunkBlock = null;
        this.maxRadius = Math.min(maxRadius, species.getMaxBranchRadius());
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            radius = primaryThickness;
            //Store the last block to be part of the trunk
            if (highestTrunkBlock == null && !TreeHelper.isBranch(level.getBlockState(pos.above())))
                highestTrunkBlock = pos;
        }

        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        //Calculate Branch Thickness based on neighboring branches

        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            float areaAccum = radius * radius;//Start by accumulating the branch we just came from
            boolean isTwig = true;

            for (Direction dir : Direction.values()) {
                if (!dir.equals(fromDir)) {//Don't count where the signal originated from

                    BlockPos dPos = pos.relative(dir);

                    if (dPos.equals(last)) {//or the branch we just came back from
                        isTwig = false;//on the return journey if the block we just came from is a branch we are obviously not the endpoint(twig)
                        continue;
                    }

                    BlockState deltaBlockState = level.getBlockState(dPos);
                    TreePart treepart = TreeHelper.getTreePart(deltaBlockState);
                    if (branch.isSameTree(treepart)) {
                        int branchRadius = treepart.getRadius(deltaBlockState);
                        areaAccum += branchRadius * branchRadius;
                    }
                }
            }

            if (isTwig) {
                //Handle leaves here
                if (leafMap != null) {
                    leafMap.setVoxel(pos, (byte) 16); // 16 (bit 5) is code for a twig.
                    SimpleVoxmap leafCluster = species.getLeavesProperties().getCellKit().getLeafCluster();
                    leafMap.blitMax(pos, leafCluster);
                }
            } else {
                //The new branch should be the square root of all of the sums of the areas of the branches coming into it.
                radius = (float) Math.sqrt(areaAccum) + tapering;

                //Ensure the branch is never inflated past it's species maximum
                if (radius > maxRadius) {
                    radius = maxRadius;
                }

                if (highestTrunkBlock != null){
                    //Ensure branches dont grow over 1 block thick if it isnt in the trunk
                    int blockRadius = 8;
                    boolean isInTrunk = (pos.getX() == highestTrunkBlock.getX() && pos.getY() <= highestTrunkBlock.getY() && pos.getZ() == highestTrunkBlock.getZ());
                    if (radius > blockRadius && !isInTrunk){
                        radius = blockRadius;
                    }
                }

                //Ensure non-twig branches are at least radius 2
                if (radius < secondaryThickness) {
                    radius = secondaryThickness;
                }

                branch.setRadius(level, pos, (int) Math.floor(radius), null);
                if (leafMap != null) {
                    leafMap.setVoxel(pos, (byte) 32); // 32 (bit 6) is code for a branch.
                }
            }

            last = pos;

        } else {
            SoilBlock rooty = TreeHelper.getRooty(state);
            if (rooty != null){
                rooty.updateRadius(level, state, pos, 2, false);
            }
        }

        return false;
    }

}
