package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.trees.Species;
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
public class DestroyerNode implements NodeInspector {

    Species species;//Destroy any node that's made of the same kind of wood
    private final List<BlockPos> endPoints;//We always need to track endpoints during destruction
    private Player player = null;

    public DestroyerNode(Species species) {
        this.endPoints = new ArrayList<>(32);
        this.species = species;
    }

    public DestroyerNode setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public List<BlockPos> getEnds() {
        return endPoints;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, @Nullable Direction fromDir) {
        if (BranchConnectables.getConnectionRadiusForBlock(blockState, world, pos, fromDir == null ? null : fromDir.getOpposite()) > 0) {
            if (player != null && world instanceof Level) {
                BlockEntity te = world.getBlockEntity(pos);
                blockState.getBlock().onDestroyedByPlayer(blockState, (Level) world, pos, player, true, world.getFluidState(pos));
                blockState.getBlock().playerDestroy((Level) world, player, pos, blockState, te, player.getMainHandItem());
            } else {
                world.setBlock(pos, BlockStates.AIR, 0);
            }
            return true;
        }

        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null && species.getFamily() == branch.getFamily()) {
            boolean waterlogged = blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED);
            if (branch.getRadius(blockState) == species.getFamily().getPrimaryThickness()) {
                endPoints.add(pos);
            }
            world.setBlock(pos, waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);//Destroy the branch and notify the client
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }
}
