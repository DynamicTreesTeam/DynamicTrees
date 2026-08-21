package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.block.entity.BlockEntityTypeRebind;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CreakingHeartBranchBlockEntity extends CreakingHeartBlockEntity {

    public CreakingHeartBranchBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(worldPosition, Blocks.CREAKING_HEART.defaultBlockState());
        ((BlockEntityTypeRebind) (Object) this).dynamictrees$rebindType(DTRegistries.CREAKING_HEART_BLOCK_ENTITY.get());
        this.setBlockState(blockState);
    }

    @Nullable
    public CreakingHeartFamily getHeartFamily(Level level) {
        CreakingHeartBranchBlock branch = getHeartBranch(level);
        if (branch == null) {
            return null;
        }
        if (branch.getFamily() instanceof CreakingHeartFamily heartFamily) {
            return heartFamily;
        }
        return null;
    }

    @Nullable
    public CreakingHeartBranchBlock getHeartBranch(Level level) {
        if (TreeHelper.getBranch(level.getBlockState(getBlockPos())) instanceof CreakingHeartBranchBlock heartBranch) {
            return heartBranch;
        }
        return null;
    }

    @Nullable
    public BranchBlock getStandardBranch(Level level) {
        Family family = getHeartFamily(level);
        if (family == null) {
            return null;
        }
        return family.getBranch().orElse(null);
    }

    public static void emitParticlesToPosition(ServerLevel level, int count, boolean towardsSource, AABB destinationBox, AABB sourceBox) {
        int color = towardsSource ? 16545810 : 6250335;
        RandomSource random = level.getRandom();
        for (double i = 0.0F; i < (double) count; ++i) {
            Vec3 source = sourceBox.getMinPosition().add(random.nextDouble() * sourceBox.getXsize(), random.nextDouble() * sourceBox.getYsize(), random.nextDouble() * sourceBox.getZsize());
            Vec3 destination = destinationBox.getMinPosition().add(random.nextDouble() * destinationBox.getXsize(), random.nextDouble() * destinationBox.getYsize(), random.nextDouble() * destinationBox.getZsize());
            if (towardsSource) {
                Vec3 swap = source;
                source = destination;
                destination = swap;
            }
            TrailParticleOption particleOption = new TrailParticleOption(destination, color, random.nextInt(40) + 10);
            level.sendParticles(particleOption, true, true, source.x, source.y, source.z, 1, 0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

}
