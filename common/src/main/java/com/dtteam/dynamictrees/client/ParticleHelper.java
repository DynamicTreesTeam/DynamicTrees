package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleHelper {

    private static void addDustParticle(Level level, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
        if (level.isClientSide()) {
            Particle particle = Minecraft.getInstance().particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
            if (particle != null) particle.setColor(r, g, b);
        }
    }

    public static void spawnParticles(Level level, SimpleParticleType particleType, BlockPos pos, int numParticles, RandomSource random) {
        spawnParticles(level, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, random);
    }

    public static void spawnParticles(LevelAccessor level, SimpleParticleType particleType, int x, int y, int z, int numParticles, RandomSource random) {
        if (level.isClientSide()) {
            for (int i1 = 0; i1 < numParticles; ++i1) {
                double mx = random.nextGaussian() * 0.02D;
                double my = random.nextGaussian() * 0.02D;
                double mz = random.nextGaussian() * 0.02D;
                level.addParticle(particleType, x + random.nextFloat(), (double) y + (double) random.nextFloat(), (double) z + random.nextFloat(), mx, my, mz);
            }
        }
    }

    private static int getFoliageColor(LeavesProperties leavesProperties, Level level, BlockState blockState, BlockPos pos) {
        return leavesProperties.foliageColorMultiplier(blockState, level, pos);
    }
    public static void crushLeavesBlock(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if (level.isClientSide()) {
            RandomSource random = level.getRandom();
            TreePart treePart = TreeHelper.getTreePart(blockState);
            if (treePart instanceof DynamicLeavesBlock leaves) {
                LeavesProperties leavesProperties = leaves.getLeavesProperties();
                int color = getFoliageColor(leavesProperties, level, blockState, pos);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;
                for (int dz = 0; dz < 8; dz++) {
                    for (int dy = 0; dy < 8; dy++) {
                        for (int dx = 0; dx < 8; dx++) {
                            if (random.nextInt(8) == 0) {
                                double fx = pos.getX() + dx / 8.0;
                                double fy = pos.getY() + dy / 8.0;
                                double fz = pos.getZ() + dz / 8.0;
                                addDustParticle(level, fx, fy, fz, 0, random.nextFloat() * entity.getDeltaMovement().y, 0, blockState, r, g, b);
                            }
                        }
                    }
                }
            }
        }
    }

}
