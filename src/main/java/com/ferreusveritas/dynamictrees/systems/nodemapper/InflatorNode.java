package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class InflatorNode implements NodeInspector {

    private float radius;
    private BlockPos last;
    private BlockPos highestTrunkBlock;
    private int maxRadius;

    Species species;
    SimpleVoxmap leafMap;

    public InflatorNode(Species species, SimpleVoxmap leafMap) {
        this(species, leafMap, species.getMaxBranchRadius());
    }
    public InflatorNode(Species species, SimpleVoxmap leafMap, int maxRadius) {
        this.species = species;
        this.leafMap = leafMap;
        last = BlockPos.ZERO;
        highestTrunkBlock = null;
        this.maxRadius = Math.min(maxRadius, species.getMaxBranchRadius());
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            radius = species.getFamily().getPrimaryThickness();
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
                radius = (float) Math.sqrt(areaAccum) + (species.getTapering() * species.getWorldGenTaperingFactor());

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
                float secondaryThickness = species.getFamily().getSecondaryThickness();
                if (radius < secondaryThickness) {
                    radius = secondaryThickness;
                }

                branch.setRadius(level, pos, (int) Math.floor(radius), null);
                if (leafMap != null) {
                    leafMap.setVoxel(pos, (byte) 32); // 32 (bit 6) is code for a branch.
                }
            }

            last = pos;

        }

        return false;
    }

}
