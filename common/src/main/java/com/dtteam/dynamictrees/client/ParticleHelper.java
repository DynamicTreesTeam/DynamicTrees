package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleHelper {

    private static void addDustParticle(Level level, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
        if (level.isClientSide()) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
//            SingleQuadParticle particle = Minecraft.getInstance().particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
//            assert particle != null;
//            particle.setColor(r, g, b);
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particleType, BlockPos pos, int numParticles, RandomSource random) {
        spawnParticles(level, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, random);
    }

    public static void spawnParticles(LevelAccessor level, ParticleOptions particleOptions, int x, int y, int z, int numParticles, RandomSource random) {
        if (level.isClientSide()) {
            for (int i1 = 0; i1 < numParticles; ++i1) {
                double mx = random.nextGaussian() * 0.02D;
                double my = random.nextGaussian() * 0.02D;
                double mz = random.nextGaussian() * 0.02D;
                level.addParticle(particleOptions, x + random.nextFloat(), (double) y + (double) random.nextFloat(), (double) z + random.nextFloat(), mx, my, mz);
            }
        }
    }

    public static void crushLeavesBlock(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if (level instanceof ClientLevel cLevel) {
            RandomSource random = cLevel.getRandom();
            TreePart treePart = TreeHelper.getTreePart(blockState);
            if (treePart instanceof DynamicLeavesBlock leaves) {
                LeavesProperties leavesProperties = leaves.getLeavesProperties();
                int color = TintSourceHelper.getFoliageColor(leavesProperties, cLevel, blockState, pos);
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
                                addDustParticle(cLevel, fx, fy, fz, 0, random.nextFloat() * entity.getDeltaMovement().y, 0, blockState, r, g, b);
                            }
                        }
                    }
                }
            }
        }
    }

}
