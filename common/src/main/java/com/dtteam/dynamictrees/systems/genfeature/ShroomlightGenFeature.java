package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.function.TetraFunction;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Gen feature for shroomlight but works for any block. Can be fully customized with a custom predicate for natural
 * growth. It is recommended for the generated block to be made connectable using {@link
 * com.dtteam.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, TetraFunction)}
 *
 * @author Max Hyper
 */
public class ShroomlightGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> SHROOMLIGHT_BLOCK = ConfigurationProperty.block("shroomlight");

    private static final Direction[] HORIZONTALS = CoordUtils.HORIZONTALS;
    private static final double VANILLA_GROW_CHANCE = .005f;

    public ShroomlightGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(SHROOMLIGHT_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, PLACE_CHANCE, MAX_COUNT);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(SHROOMLIGHT_BLOCK, Blocks.SHROOMLIGHT)
                .with(MAX_HEIGHT, 32)
                .with(CAN_GROW_PREDICATE, (level, blockPos) ->
                        level.getRandom().nextFloat() <= VANILLA_GROW_CHANCE)
                .with(PLACE_CHANCE, .3f)
                .with(MAX_COUNT, 4);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        boolean placed = this.placeShroomlightsInValidPlace(configuration, context.level(), context.pos(), true);
        if (placed){
            //pulse the lower half of the
            SimpleVoxmap leafMap = context.species().getLeavesProperties().getCellKit().getLeafCluster();
            for (BlockPos endPos : context.endPoints()){
                TreeHelper.ageVolume(context.level(), endPos.below(leafMap.getLenY()/2 +1), leafMap.getLenX() / 2, leafMap.getLenY() / 2 -1, 4, true);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        return context.natural() && configuration.get(CAN_GROW_PREDICATE).test(context.level(), context.pos().above())
                && context.fertility() != 0 && this.placeShroomlightsInValidPlace(configuration, context.level(), context.pos(), false);
    }

    private boolean placeShroomlightsInValidPlace(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos, boolean worldGen) {
        int treeHeight = getTreeHeight(level, rootPos, configuration.get(MAX_HEIGHT));
        Block shroomlightBlock = configuration.get(SHROOMLIGHT_BLOCK);

        List<BlockPos> validSpaces = findBranchPits(configuration, level, rootPos, treeHeight);
        if (validSpaces == null) {
            return false;
        }
        if (!validSpaces.isEmpty()) {
            if (worldGen) {
                int placed = 0;
                for (BlockPos chosenSpace : validSpaces) {
                    if (level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE)) {
                        level.setBlock(chosenSpace, shroomlightBlock.defaultBlockState(), 2);
                        placed++;
                        if (placed > configuration.get(MAX_COUNT)) {
                            break;
                        }
                    }
                }
            } else {
                BlockPos chosenSpace = validSpaces.get(level.getRandom().nextInt(validSpaces.size()));
                level.setBlock(chosenSpace, shroomlightBlock.defaultBlockState(), 2);
            }
            return true;
        }
        return false;
    }

    private int getTreeHeight(LevelAccessor level, BlockPos rootPos, int maxHeight) {
        for (int i = 1; i < maxHeight; i++) {
            if (!TreeHelper.isBranch(level.getBlockState(rootPos.above(i)))) {
                return i - 1;
            }
        }
        return maxHeight;
    }

    //Like the BeeNestGenFeature, the valid places are empty blocks under branches next to the trunk.
    @Nullable
    private List<BlockPos> findBranchPits(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos, int maxHeight) {
        int existingBlocks = 0;
        List<BlockPos> validSpaces = new LinkedList<>();
        boolean firstPit = true;
        for (int y = 2; y < maxHeight; y++) {
            BlockPos trunkPos = rootPos.above(y);
            for (Direction dir : HORIZONTALS) {
                BlockPos sidePos = trunkPos.relative(dir);
                if ((level.isEmptyBlock(sidePos) || level.getBlockState(sidePos).getBlock() instanceof DynamicLeavesBlock) && TreeHelper.isBranch(level.getBlockState(sidePos.above()))) {
                    //skip the first layer so shroomlights don't block the wart
                    if (firstPit) {
                        firstPit = false;
                        break;
                    }
                    validSpaces.add(sidePos);
                } else if (level.getBlockState(sidePos).getBlock() == configuration.get(SHROOMLIGHT_BLOCK)) {
                    existingBlocks++;
                    if (existingBlocks > configuration.get(MAX_COUNT)) {
                        return null;
                    }
                }
            }
        }
        return validSpaces;
    }

}
