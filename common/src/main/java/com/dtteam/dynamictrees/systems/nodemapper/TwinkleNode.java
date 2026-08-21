package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.client.ParticleHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
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

    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        if (level.isClientSide() && TreeHelper.isBranch(state)) {
            ParticleHelper.spawnParticles(level, this.particleType, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, level.getRandom());
        }
        return false;
    }

    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
