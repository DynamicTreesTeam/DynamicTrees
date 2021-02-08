package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Gen feature for shroomlight but works for any block.
 * Can be fully customized with a custom predicate for natural growth.
 * It is recommended for the generated block to be made connectable using {@link com.ferreusveritas.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, BranchConnectables.RadiusForConnectionFunction) makeBlockConnectable}
 *
 * @author Max Hyper
 */
public class ShroomlightGenFeature implements IPostGenFeature, IPostGrowFeature {

    Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private Block shroomlightBlock;
    private int maxHeight = 32;
    private BiPredicate<World, BlockPos> canGrowPredicate;
    private double worldgenPlaceChance;

    private static final double vanillaGrowChance = 0.005f;

    public ShroomlightGenFeature () {
        this(Blocks.SHROOMLIGHT, 0.4f, (world, blockPos) -> world.getRandom().nextFloat() <= vanillaGrowChance);
    }

    public ShroomlightGenFeature (Block shroomlightBlock, double worldgenPlaceChance, BiPredicate<World, BlockPos> canGrow) {
        this.shroomlightBlock = shroomlightBlock;
        this.worldgenPlaceChance = worldgenPlaceChance;
        this.canGrowPredicate = canGrow;
    }

    @Override
    public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        return placeShroomlightsInValidPlace(world, rootPos, true);
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (!natural || !canGrowPredicate.test(world, rootPos.up())) return false;

        return placeShroomlightsInValidPlace(world, rootPos, false);
    }

    public void setCanGrowPredicate (BiPredicate<World, BlockPos> predicate){ this.canGrowPredicate = predicate; }
    public void setMaxHeight (int maxHeight){
        this.maxHeight = maxHeight;
    }

    private boolean placeShroomlightsInValidPlace(IWorld world, BlockPos rootPos, boolean worldGen){
        int treeHeight = getTreeHeight(world, rootPos);

        List<BlockPos> validSpaces = findBranchPits(world, rootPos, treeHeight);
        if (validSpaces.size() > 0){
            if (worldGen){
                for (BlockPos chosenSpace : validSpaces){
                    if (world.getRandom().nextFloat() <= worldgenPlaceChance)
                        world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
                }
            } else {
                BlockPos chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
                world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
            }
            return true;
        }
        return false;
    }

    private int getTreeHeight (IWorld world, BlockPos rootPos){
        for (int i = 1; i<maxHeight; i++){
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))){
                return i-1;
            }
        }
        return maxHeight;
    }

    //Like the BeeNestGenFeature, the valid places are empty blocks under branches next to the trunk.
    private List<BlockPos> findBranchPits (IWorld world, BlockPos rootPos, int maxHeight){
        List<BlockPos> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++){
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : HORIZONTALS){
                BlockPos sidePos = trunkPos.offset(dir);
                if ((world.isAirBlock(sidePos) || world.getBlockState(sidePos).getBlock() instanceof DynamicLeavesBlock) && TreeHelper.isBranch(world.getBlockState(sidePos.up())))
                    validSpaces.add(sidePos);
            }
        }
        return validSpaces;
    }

}
