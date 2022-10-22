package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PodGenerationNode implements NodeInspector {

    public interface PodPlacer {
        void place(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue, Direction facing);
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
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {

        if (!finished) {
            int hashCode = CoordUtils.coordHashCode(pos, 1);
            if ((hashCode % 97) % 29 == 0) {
                BranchBlock branch = TreeHelper.getBranch(state);
                if (branch != null && branch.getRadius(state) == 8) {
                    int side = (hashCode % 4) + 2;
                    Direction dir = Direction.from3DDataValue(side);
                    BlockPos deltaPos = pos.relative(dir);
                    if (level.isEmptyBlock(deltaPos)) {
                        if (!dir.getAxis().isHorizontal()) {
                            dir = Direction.NORTH;
                        }
                        podPlacer.place(level, deltaPos, seasonValue, dir.getOpposite());
                    }
                } else {
                    finished = true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
