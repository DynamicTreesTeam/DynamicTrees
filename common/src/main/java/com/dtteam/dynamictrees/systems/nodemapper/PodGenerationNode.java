package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class PodGenerationNode implements NodeInspector {

    public interface PodPlacer {
        void place(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue, Direction facing, int radius);
    }

    private final Pod pod;
    private final PodPlacer podPlacer;
    @Nullable
    private final Float seasonValue;
    private final int blocksPerPlacedPod;
    private final BlockPos rootPos;
    private final int lowestTrunkHeight;

    public PodGenerationNode(Pod pod, PodPlacer podPlacer, @Nullable Float seasonValue, int blocksPerPlacedPods, BlockPos rootPos, int lowestTrunkHeight) {
        this.podPlacer = podPlacer;
        this.seasonValue = seasonValue;
        this.blocksPerPlacedPod = blocksPerPlacedPods;
        this.pod = pod;
        this.rootPos = rootPos;
        this.lowestTrunkHeight = lowestTrunkHeight;
    }

    private boolean finished = false;

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {

        if (!finished) {
            if (pos.getY() - rootPos.above().getY() < lowestTrunkHeight) return false;
            BranchBlock branch = TreeHelper.getBranch(state);
            if (branch != null) {
                int radius = branch.getRadius(state);
                if (pod.isValidRadius(radius)){
                    for (Direction dir : Direction.Plane.HORIZONTAL){
                        int hashCode = CoordUtils.coordHashCode(pos.offset(dir.getNormal()), 1);
                        if ((hashCode % 97) % blocksPerPlacedPod == 0) {
                            BlockPos deltaPos = pos.relative(dir);
                            if (level.isEmptyBlock(deltaPos) || level.getBlockState(deltaPos).getBlock() instanceof TrunkShellBlock) {
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
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
