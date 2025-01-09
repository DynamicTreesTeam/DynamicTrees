package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.util.BlockStates;
import com.dtteam.dynamictrees.util.CoordUtils;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PodzolGenFeature extends GenFeature {

    public PodzolGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (!Services.CONFIG.getBoolConfig("podzolGen")) {
            return false;
        }

        final LevelAccessor level = context.level();
        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(level, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();

        if (endPoints.isEmpty()) {
            return false;
        }

        final RandomSource random = context.random();
        final BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

        final int x = pos.getX() + random.nextInt(5) - 2;
        final int z = pos.getZ() + random.nextInt(5) - 2;

        final int darkThreshold = 4;

        for (int i = 0; i < 32; i++) {
            final BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

            if (!level.isEmptyBlock(offPos)) {
                final BlockState state = level.getBlockState(offPos);
                final Block block = state.getBlock();

                // Skip past Mushrooms and branches on the way down.
                if (block instanceof BranchBlock || block instanceof MushroomBlock || block instanceof LeavesBlock) {
                    continue;
                } else if (block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock) {
                    // Kill plants.
                    if (level.getBrightness(LightLayer.SKY, offPos) <= darkThreshold) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    continue;
                } else if (SoilHelper.isSoilAcceptable(state, SoilHelper.getSoilFlags(SoilHelper.DIRT_LIKE))) {
                    // Convert grass or dirt to podzol.
                    if (level.getBrightness(LightLayer.SKY, offPos.above()) <= darkThreshold) {
                        level.setBlock(offPos, BlockStates.PODZOL, Block.UPDATE_ALL);
                    } else {
                        spreadPodzol(level, pos);
                    }
                }
                break;
            }
        }
        return true;
    }

    public static void spreadPodzol(LevelAccessor level, BlockPos pos) {
        int podzolish = 0;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockPos deltaPos = pos.relative(dir);
            Block testBlock = level.getBlockState(deltaPos).getBlock();
            podzolish += (testBlock == Blocks.PODZOL) ? 1 : 0;
            podzolish += testBlock instanceof SoilBlock ? 1 : 0;
            if (podzolish >= 3) {
                level.setBlock(pos, BlockStates.PODZOL, Block.UPDATE_ALL);
                break;
            }
        }
    }

}
