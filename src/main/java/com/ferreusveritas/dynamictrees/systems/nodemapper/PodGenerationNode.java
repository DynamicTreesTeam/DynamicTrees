package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PodGenerationNode implements NodeInspector {

    public interface PodPlacer {
        void place(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue, Direction facing, int radius);
    }

    private final Pod pod;
    private final PodPlacer podPlacer;
    @Nullable
    private final Float seasonValue;
    private final int blocksPerPlacedPod;

    public PodGenerationNode(Pod pod, PodPlacer podPlacer, @Nullable Float seasonValue, int blocksPerPlacedPods) {
        this.podPlacer = podPlacer;
        this.seasonValue = seasonValue;
        this.blocksPerPlacedPod = blocksPerPlacedPods;
        this.pod = pod;
    }

    private boolean finished = false;

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {

        if (!finished) {
            //int hashCode = CoordUtils.coordHashCode(pos, 1);
            //if ((hashCode % 97) % blocksPerPlacedPod == 0) {
                BranchBlock branch = TreeHelper.getBranch(state);
                if (branch != null) {
                    int radius = branch.getRadius(state);
                    if (pod.isValidRadius(radius)){
                        //int side = (hashCode % 4) + 2;
                        //Direction dir = Direction.from3DDataValue(side);
                        for (Direction dir : Direction.Plane.HORIZONTAL){
                            int hashCode = CoordUtils.coordHashCode(pos.offset(dir.getNormal()), 1);
                            if ((hashCode % 97) % blocksPerPlacedPod == 0) {
                                BlockPos deltaPos = pos.relative(dir);
                                if (level.isEmptyBlock(deltaPos)) {
                                    if (!dir.getAxis().isHorizontal()) {
                                        dir = Direction.NORTH;
                                    }
                                    podPlacer.place(level, deltaPos, seasonValue, dir.getOpposite(), radius);
                                    finished = true;
                                }
                            }

                        }
                    }
                }
            //}
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
