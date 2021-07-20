package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.TetraFunction;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Gen feature for shroomlight but works for any block.
 * Can be fully customized with a custom predicate for natural growth.
 * It is recommended for the generated block to be made connectable using {@link com.ferreusveritas.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, TetraFunction)}
 *
 * @author Max Hyper
 */
public class ShroomlightGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> SHROOMLIGHT_BLOCK = ConfigurationProperty.block("shroomlight");

    private static final Direction[] HORIZONTALS = CoordUtils.HORIZONTALS;
    private static final double VANILLA_GROW_CHANCE = .005f;

    public ShroomlightGenFeature (ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(SHROOMLIGHT_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, PLACE_CHANCE, MAX_COUNT);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(SHROOMLIGHT_BLOCK, Blocks.SHROOMLIGHT)
                .with(MAX_HEIGHT, 32)
                .with(CAN_GROW_PREDICATE, (world, blockPos) ->
                        world.getRandom().nextFloat() <= VANILLA_GROW_CHANCE)
                .with(PLACE_CHANCE, .4f)
                .with(MAX_COUNT, 4);
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature<GenFeature> configuration, PostGenerationContext context) {
        return this.placeShroomlightsInValidPlace(configuration, context.world(), context.pos(), true);
    }

    @Override
    protected boolean postGrow(ConfiguredGenFeature<GenFeature> configuration, PostGrowContext context) {
        return context.natural() && configuration.get(CAN_GROW_PREDICATE).test(context.world(), context.pos().above())
                && context.fertility() != 0 && this.placeShroomlightsInValidPlace(configuration, context.world(), context.pos(), false);
    }

    private boolean placeShroomlightsInValidPlace(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, boolean worldGen){
        int treeHeight = getTreeHeight(world, rootPos, configuredGenFeature.get(MAX_HEIGHT));
        Block shroomlightBlock = configuredGenFeature.get(SHROOMLIGHT_BLOCK);

        List<BlockPos> validSpaces = findBranchPits(configuredGenFeature, world, rootPos, treeHeight);
        if (validSpaces == null) return false;
        if (validSpaces.size() > 0){
            if (worldGen){
                int placed = 0;
                for (BlockPos chosenSpace : validSpaces){
                    if (world.getRandom().nextFloat() <= configuredGenFeature.get(PLACE_CHANCE)){
                        world.setBlock(chosenSpace, shroomlightBlock.defaultBlockState(), 2);
                        placed++;
                        if (placed > configuredGenFeature.get(MAX_COUNT)) break;
                    }
                }
            } else {
                BlockPos chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
                world.setBlock(chosenSpace, shroomlightBlock.defaultBlockState(), 2);
            }
            return true;
        }
        return false;
    }

    private int getTreeHeight (IWorld world, BlockPos rootPos, int maxHeight){
        for (int i = 1; i < maxHeight; i++) {
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.above(i)))){
                return i-1;
            }
        }
        return maxHeight;
    }

    //Like the BeeNestGenFeature, the valid places are empty blocks under branches next to the trunk.
    @Nullable
    private List<BlockPos> findBranchPits (ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, int maxHeight){
        int existingBlocks = 0;
        List<BlockPos> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++){
            BlockPos trunkPos = rootPos.above(y);
            for (Direction dir : HORIZONTALS){
                BlockPos sidePos = trunkPos.relative(dir);
                if ((world.isEmptyBlock(sidePos) || world.getBlockState(sidePos).getBlock() instanceof DynamicLeavesBlock) && TreeHelper.isBranch(world.getBlockState(sidePos.above())))
                    validSpaces.add(sidePos);
                else if (world.getBlockState(sidePos).getBlock() == configuredGenFeature.get(SHROOMLIGHT_BLOCK)) {
                    existingBlocks ++;
                    if (existingBlocks > configuredGenFeature.get(MAX_COUNT)) return null;
                }
            }
        }
        return validSpaces;
    }

}
