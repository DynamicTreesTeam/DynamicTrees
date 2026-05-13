package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LeafLitterGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> LEAF_LITTER_BLOCK = ConfigurationProperty.block("leaf_litter");
    public static final ConfigurationProperty<Integer> SPREAD_DISTANCE = ConfigurationProperty.integer("spread_distance");
    public static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");
    public static final ConfigurationProperty<Integer> MAX_LITTER_AROUND = ConfigurationProperty.integer("max_litter_around");
    public static final ConfigurationProperty<Integer> MAX_LITTER_AMOUNT = ConfigurationProperty.integer("max_litter_amount");

    public LeafLitterGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        register(LEAF_LITTER_BLOCK, MAX_HEIGHT, MAX_LITTER_AROUND, SPREAD_DISTANCE, QUANTITY, MAX_LITTER_AMOUNT);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MAX_LITTER_AROUND, 1)
                .with(LEAF_LITTER_BLOCK, Blocks.LEAF_LITTER)
                .with(SPREAD_DISTANCE, 3)
                .with(MAX_HEIGHT, 32)
                .with(QUANTITY, 24)
                .with(MAX_LITTER_AMOUNT, 4);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.endPoints().isEmpty()) {
            final LevelAccessor level = context.level();
            final RandomSource random = context.random();
            int qty = configuration.get(QUANTITY);
            Block leaf_litter = configuration.get(LEAF_LITTER_BLOCK);
            int maxAmount = Math.clamp(configuration.get(MAX_LITTER_AMOUNT), 1, 4);

            for (int j = 0; j < qty; j++) {
                final BlockPos pos = context.endPoints().get(context.random().nextInt(context.endPoints().size()));

                BlockPos placePos = getPlacePos(configuration, pos, level, random);
                if (placePos != null){
                    placeLeafLitter(level, placePos, leaf_litter, random, Block.UPDATE_CLIENTS, maxAmount);
                }
            }
            return true;
        }
        return false;
    }


    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() == 0) return false;

        final LevelAccessor level = context.level();
        final RandomSource random = context.random();
        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(level, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();
        if (endPoints.isEmpty()) return false;

        final BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

        Block leafLitter = configuration.get(LEAF_LITTER_BLOCK);
        BlockPos placePos = getPlacePos(configuration, pos, level, random);
        int maxAmount = Math.clamp(configuration.get(MAX_LITTER_AMOUNT), 1, 4);
        if (placePos != null){
            int surroundingLitter = countLitterAround(placePos, level, leafLitter);
            if (surroundingLitter > configuration.get(MAX_LITTER_AROUND)) return false;
            placeLeafLitter(level, placePos, leafLitter, random, Block.UPDATE_ALL, maxAmount);
        }
        return true;
    }

    private static int countLitterAround(BlockPos placePos, LevelAccessor level, Block leafLitter) {
        int surroundingLitter = 0;
        for (Direction dir : CoordUtils.HORIZONTALS){
            BlockPos side = placePos.offset(dir.getUnitVec3i());
            if (level.getBlockState(side).is(leafLitter)) surroundingLitter ++;
        }
        return surroundingLitter;
    }

    @Nullable
    private BlockPos getPlacePos(GenFeatureConfiguration configuration, BlockPos pos, LevelAccessor level, RandomSource random) {
        int maxHeight = configuration.get(MAX_HEIGHT);
        int distance = configuration.get(SPREAD_DISTANCE);
        final int x = pos.getX() + random.nextInt(distance * 2 + 1) - distance;
        final int z = pos.getZ() + random.nextInt(distance * 2 + 1) - distance;

        for (int i = 0; i < maxHeight; i++) {
            final BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

            final BlockState offState = level.getBlockState(offPos);
            if (level.isEmptyBlock(offPos)) continue;

            if (SoilHelper.isSoilAcceptable(offState, SoilHelper.getSoilFlags(SoilHelper.DIRT_LIKE))) {
                return offPos.above();
            }
            break;
        }
        return null;
    }

    private static void placeLeafLitter(LevelAccessor level, BlockPos placePos, Block leaf_litter, RandomSource rand, int flags, int maxAmount) {
        BlockState state = leaf_litter.defaultBlockState();
        if (state.hasProperty(LeafLitterBlock.FACING)){
            state = state.setValue(LeafLitterBlock.FACING, CoordUtils.getRandom2DDir(rand));
        }
        if (state.hasProperty(LeafLitterBlock.AMOUNT)){
            int count = 1 + rand.nextInt(maxAmount);
            //If it lands on a full pile we have a chance to reroll. This is to more closely replicate vanilla's look
            if (count == maxAmount && rand.nextBoolean())
                count = 1 + rand.nextInt(maxAmount);
            state = state.setValue(LeafLitterBlock.AMOUNT, count);
        }

        level.setBlock(placePos, state, flags);
    }

}
