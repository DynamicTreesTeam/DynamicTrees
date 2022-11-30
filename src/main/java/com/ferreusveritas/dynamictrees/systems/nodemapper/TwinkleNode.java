package com.ferreusveritas.dynamictrees.systems.nodemapper;

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
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        if (level.isClientSide() && TreeHelper.isBranch(state)) {
            DTClient.spawnParticles(level, this.particleType, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, level.getRandom());
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
