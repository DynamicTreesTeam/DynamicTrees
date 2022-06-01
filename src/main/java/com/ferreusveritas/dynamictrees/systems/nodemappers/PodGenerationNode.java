package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public class PodGenerationNode implements NodeInspector {

    public interface PodPlacer {
        void place(IWorld world, BlockPos pos, @Nullable Float seasonValue, Direction facing);
    }

    private final PodPlacer podPlacer;
    @Nullable
    private final Float seasonValue;

    public PodGenerationNode(PodPlacer podPlacer, @Nullable Float seasonValue) {
        this.podPlacer = podPlacer;
        this.seasonValue = seasonValue;
    }

    private boolean finished = false;

    @Override
    public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {

        if (!finished) {
            int hashCode = CoordUtils.coordHashCode(pos, 1);
            if ((hashCode % 97) % 29 == 0) {
                BranchBlock branch = TreeHelper.getBranch(blockState);
                if (branch != null && branch.getRadius(blockState) == 8) {
                    int side = (hashCode % 4) + 2;
                    Direction dir = Direction.from3DDataValue(side);
                    BlockPos deltaPos = pos.relative(dir);
                    if (world.isEmptyBlock(deltaPos)) {
                        if (!dir.getAxis().isHorizontal()) {
                            dir = Direction.NORTH;
                        }
                        podPlacer.place(world, deltaPos, seasonValue, dir.getOpposite());
                    }
                } else {
                    finished = true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
