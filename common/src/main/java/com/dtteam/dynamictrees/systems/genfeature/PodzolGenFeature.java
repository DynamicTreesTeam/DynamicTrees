package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PodzolGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> PODZOL_BLOCK = ConfigurationProperty.block("podzol_block");
    public static final ConfigurationProperty<Integer> SPREAD_DISTANCE = ConfigurationProperty.integer("spread_distance");
    public static final ConfigurationProperty<Integer> DARK_THRESHOLD = ConfigurationProperty.integer("dark_threshold");
    public static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");
    public static final ConfigurationProperty<Boolean> KILL_PLANTS = ConfigurationProperty.bool("kill_plants");

    public PodzolGenFeature(Identifier registryName) {
        super(registryName);
    }

    protected void registerProperties() {
        register(KILL_PLANTS, DARK_THRESHOLD, SPREAD_DISTANCE, PODZOL_BLOCK, MAX_HEIGHT);
    }

    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(KILL_PLANTS, false)
                .with(DARK_THRESHOLD, 10)
                .with(SPREAD_DISTANCE, 2)
                .with(PODZOL_BLOCK, Blocks.PODZOL)
                .with(MAX_HEIGHT, 32);
    }

    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (!DTConfigs.SERVER.generatePodzol.get()) return false;

        final LevelAccessor level = context.level();
        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(level, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();
        if (endPoints.isEmpty()) return false;

        final RandomSource random = context.random();
        final BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

        int distance = configuration.get(SPREAD_DISTANCE);
        final int x = pos.getX() + random.nextInt(distance * 2 + 1) - distance;
        final int z = pos.getZ() + random.nextInt(distance * 2 + 1) - distance;

        final int darkThreshold = configuration.get(DARK_THRESHOLD);
        Block podzol = configuration.get(PODZOL_BLOCK);
        int maxHeight = configuration.get(MAX_HEIGHT);
        for (int i = 0; i < maxHeight; i++) {
            final BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

            if (level.isEmptyBlock(offPos)) continue;
            final BlockState state = level.getBlockState(offPos);

            if (TreeHelper.isRooty(state)){
                break; //Don't try to turn rooty soil into podzol.
            }

            if (configuration.get(KILL_PLANTS) && shouldDestroyPlant(state)) {
                // Kill plants.
                if (level.getBrightness(LightLayer.SKY, offPos) <= darkThreshold) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                continue;
            }

            if (state.is(DTBlockTags.FOLIAGE) || TreeHelper.isTreePart(state)) {
                // Skip past Foliage and branches on the way down.
                continue;
            }

            if (SoilHelper.isSoilAcceptable(state, SoilHelper.getSoilFlags(SoilHelper.DIRT_LIKE))) {
                // Convert grass or dirt to podzol.
                if (level.getBrightness(LightLayer.SKY, offPos.above()) <= darkThreshold) {
                    level.setBlock(offPos, podzol.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    spreadPodzol(level, pos, podzol);
                }
            }

            break;
        }
        return true;
    }

    private boolean shouldDestroyPlant(BlockState state){
        Block block = state.getBlock();
        return block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock;
    }

    public static void spreadPodzol(LevelAccessor level, BlockPos pos, Block podzol) {
        int podzolish = 0;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockPos deltaPos = pos.relative(dir);
            Block testBlock = level.getBlockState(deltaPos).getBlock();
            podzolish += (testBlock == podzol) ? 1 : 0;
            podzolish += testBlock instanceof SoilBlock ? 1 : 0;
            if (podzolish >= 3) {
                level.setBlock(pos, podzol.defaultBlockState(), Block.UPDATE_ALL);
                break;
            }
        }
    }

}
