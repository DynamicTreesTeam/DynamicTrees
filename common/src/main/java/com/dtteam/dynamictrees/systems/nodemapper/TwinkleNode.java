package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.client.ParticleHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TwinkleNode implements NodeInspector {

    private final ParticleOptions particleOptions;
    private final int numParticles;

    public TwinkleNode(ParticleOptions type, int num) {
        particleOptions = type;
        numParticles = num;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        if (level.isClientSide() && TreeHelper.isBranch(state)) {
            ParticleHelper.spawnParticles(level, this.particleOptions, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, level.getRandom());
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
