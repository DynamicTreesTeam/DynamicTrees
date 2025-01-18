package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.BranchConnectables;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.BlockStates;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Destroys all branches on a tree and the surrounding leaves.
 *
 * @author ferreusveritas
 */
public class DestroyerNode extends FindEndsNode {

    Species species;//Destroy any node that's made of the same kind of wood
    private Player player = null;

    public DestroyerNode(Species species) {
        super();
        this.species = species;
    }

    public DestroyerNode setPlayer(Player player) {
        this.player = player;
        return this;
    }
    @Override
    public boolean run(BlockState state, LevelAccessor accessor, BlockPos pos, @Nullable Direction fromDir) {
        if (BranchConnectables.getConnectionRadiusForBlock(state, accessor, pos, fromDir == null ? null : fromDir.getOpposite()) > 0) {
            if (player != null && accessor instanceof Level level) {
                BlockEntity te = accessor.getBlockEntity(pos);
                Services.INTERACTION.blockDestroyByPlayer(state, level, pos, player, false, level.getFluidState(pos));
                state.getBlock().playerDestroy(level, player, pos, state, te, player.getMainHandItem());
            } else accessor.setBlock(pos, BlockStates.AIR, 0);
        }

        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null && species.getFamily() == branch.getFamily()) {
            accessor.setBlock(pos, branch.getStateForDecay(state, accessor, pos), 3);//Destroy the branch and notify the client
        }

        return super.run(state, accessor, pos, fromDir);
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return super.returnRun(state, level, pos, fromDir);
    }
}
