package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir) {
        if (BranchConnectables.getConnectionRadiusForBlock(state, level, pos, fromDir == null ? null : fromDir.getOpposite()) > 0) {
            if (player != null && level instanceof Level) {
                BlockEntity te = level.getBlockEntity(pos);
                state.getBlock().onDestroyedByPlayer(state, (Level) level, pos, player, true, level.getFluidState(pos));
                state.getBlock().playerDestroy((Level) level, player, pos, state, te, player.getMainHandItem());
            } else {
                level.setBlock(pos, BlockStates.AIR, 0);
            }
        }

        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null && species.getFamily() == branch.getFamily()) {
            boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);

            level.setBlock(pos, waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);//Destroy the branch and notify the client
        }

        return super.run(state, level, pos, fromDir);
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return super.returnRun(state, level, pos, fromDir);
    }
}
