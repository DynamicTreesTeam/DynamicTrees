package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TwinkleNode implements NodeInspector {

    private final SimpleParticleType particleType;
    private final int numParticles;

    public TwinkleNode(SimpleParticleType type, int num) {
        particleType = type;
        numParticles = num;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        if (world.isClientSide() && TreeHelper.isBranch(blockState)) {
            DTClient.spawnParticles(world, this.particleType, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, world.getRandom());
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
