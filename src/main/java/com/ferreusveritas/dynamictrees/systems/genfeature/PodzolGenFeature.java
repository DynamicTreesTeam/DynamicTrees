package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemapper.FindEndsNode;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.TallGrassBlock;

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
        if (!DTConfigs.PODZOL_GEN.get()) {
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
                final Block block = level.getBlockState(offPos).getBlock();

                // Skip past Mushrooms and branches on the way down.
                if (block instanceof BranchBlock || block instanceof MushroomBlock || block instanceof LeavesBlock) {
                    continue;
                } else if (block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock) {
                    // Kill plants.
                    if (level.getBrightness(LightLayer.SKY, offPos) <= darkThreshold) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    continue;
                } else if (block == Blocks.DIRT || block == Blocks.GRASS) {
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
            podzolish += testBlock instanceof RootyBlock ? 1 : 0;
            if (podzolish >= 3) {
                level.setBlock(pos, BlockStates.PODZOL, Block.UPDATE_ALL);
                break;
            }
        }
    }

}
