package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.api.cell.Cell;
import com.dtteam.dynamictrees.api.cell.CellNull;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PalmBranchBlock extends BasicBranchBlock{

    public PalmBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        if (getRadius(state) != getFamily().getPrimaryThickness()) return CellNull.NULL_CELL;
        return super.getHydrationCell(level, pos, state, dir, leavesProperties);
    }

    public GrowSignal growIntoAir(Level world, BlockPos pos, GrowSignal signal, int fromRadius) {
        final Species species = signal.getSpecies();

        final DynamicLeavesBlock leaves = species.getLeavesBlock().orElse(null);
        if (leaves != null) {
            if (fromRadius == getFamily().getPrimaryThickness()) {// If we came from a twig (and we're not a stripped branch) then just make some leaves
                if (isNextToBranch(world, pos, signal.dir.getOpposite())){
                    signal.success = false;
                    return signal;
                }
                signal.success = 0 != leaves.growLeavesIfLocationIsSuitable(world, species.getLeavesProperties(), pos.above(), 0);
                if (signal.success)
                    return leaves.branchOut(world, pos, signal);
            } else {// Otherwise make a proper branch
                return leaves.branchOut(world, pos, signal);
            }
        }
        return super.growIntoAir(world, pos, signal, fromRadius);
    }

}
