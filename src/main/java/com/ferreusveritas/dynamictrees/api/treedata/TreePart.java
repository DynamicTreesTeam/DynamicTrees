package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface TreePart {

    /**
     * Returns an {@link Cell} that provides the level of hydration to neighboring structures.
     *
     * @param level            Read-only access to blocks from an {@link BlockGetter}.
     * @param pos              The {@link BlockPos} of block at which to gather surrounding cells.
     * @param state            The {@link BlockState} of the block we are gathering the cell for.
     * @param dir              The {@link Direction} of the request (the opposite direction of the requester).
     * @param leavesProperties The {@link LeavesProperties} for the leaves the request came from.
     * @return The {@link Cell} for getting hydration levels.
     */
    Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties);

    /**
     * The signal that is passed from the root of the tree to the tip of a branch to create growth.
     *
     * @param level  The current {@link Level} instance.
     * @param pos    The {@link BlockPos} of this {@link TreePart}.
     * @param signal The {@link GrowSignal} that keeps track of the growth path.
     * @return The {@link GrowSignal} specified by the {@code signal} parameter for chaining.
     */
    GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal);

    /**
     * Returns the probability that the branch logic will follow into this block as part of it's path.
     *
     * @param state  The {@link BlockState} of this {@link TreePart}.
     * @param level  Read-only access to blocks from an {@link BlockGetter}.
     * @param pos    The {@link BlockPos} of this {@link TreePart}.
     * @param from   The {@link BranchBlock} making the request.
     * @return The probability weight used to determine if the growth path will take this block as a path next.
     */
    int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from);

    /**
     * Returns the radius of the {@link TreePart} that a neighbor is expected to connect with.
     *
     * @param state      The {@link BlockState} of this {@link TreePart}.
     * @param level      Read-only access to blocks from an {@link BlockGetter}.
     * @param pos        The {@link BlockPos} of this {@link TreePart}.
     * @param from       The {@link BranchBlock} making the request.
     * @param side       The {@link Direction} the block is requesting (relative to the requesting block).
     * @param fromRadius The radius of the {@link BranchBlock} requesting connection data.
     * @return The radius of the connection point to this block from the branch.
     */
    int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius);

    /**
     * Returns the radius of a branch, or {@code 0} if this {@link TreePart} is not a {@link BranchBlock}.
     *
     * @param state the {@link BlockState} of the block we are trying to get the radius of.
     * @return The radius of the branch, or {@code 0} if this part is not a branch.
     */
    int getRadius(BlockState state);

    /**
     * Returns whether or not this node should be analyzed. Branches should always return {@code true} where leaves
     * should always return {@code false}. Other types may vary in return depending on implementation.
     *
     * @param state  The {@link BlockState} of the block to be analysed.
     * @param level  The {@link BlockGetter} instance.
     * @param pos    The {@link BlockPos} of the block to be analysed.
     * @return {@code true} if this {@link TreePart} should be analysed; {@code false} otherwise.
     */
    boolean shouldAnalyse(BlockState state, BlockGetter level, BlockPos pos);

    /**
     * Configurable, general-purpose branch network scanner to gather data and/or perform operations on a dynamic tree.
     *
     * @param state   The {@link BlockState} of this {@link TreePart}.
     * @param level   The current {@link LevelAccessor} instance.
     * @param pos     The {@link BlockPos} of this {@link TreePart}.
     * @param fromDir The {@link Direction} that should not be analyzed; {@code null} to analyse in all directions.
     * @param signal  The {@link MapSignal} object to gather data and/or perform operations.
     * @return The specified {@link MapSignal} parameter for chaining.
     */
    MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal);

    /**
     * Returns the {@link Family} this block is used to build.
     *
     * @param state  The {@link BlockState} of this {@link TreePart}.
     * @param level  The {@link BlockGetter} instance.
     * @param pos    The {@link BlockPos} of this {@link TreePart}.
     * @return The {@link Family} this {@link TreePart} belongs to.
     */
    Family getFamily(BlockState state, BlockGetter level, BlockPos pos);

    /**
     * A branch requires 2 or more adjacent supporting neighbors, at least one of which must be another branch. Valid
     * supports are other branches (always), leaves (for twigs), and rooty dirt (under special circumstances).
     * <p>
     * The return value is nybbled neighbor values. High Nybble (0xF0) is count of branches only, Low Nybble (0x0F) is
     * any valid reinforcing {@link TreePart} (including branches).
     *
     * @param state  The {@link BlockState} of this {@link TreePart}.
     * @param level  The {@link BlockGetter} instance.
     * @param branch The {@link BranchBlock} making the request.
     * @param pos    The {@link BlockPos} of this {@link TreePart}.
     * @param dir    The {@link Direction} of the request (opposite to the direction of the requester).
     * @param radius The radius of the {@link BranchBlock} requesting support.
     * @return The neighbor values in Nybble pair ( (#branches & 0xF0) | (#treeparts & 0x0F) ).
     */
    int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius);

    enum TreePartType {
        /**
         * Not an official part of a tree.
         */
        NULL,
        /**
         * A rooty block, extending {@link RootyBlock}.
         */
        ROOT,
        /**
         * A branch block, extending {@link BranchBlock}.
         */
        BRANCH,
        /**
         * A leaves block, extending {@link DynamicLeavesBlock}.
         */
        LEAVES,
        /**
         * Another part of the tree.
         */
        OTHER
    }

    TreePartType getTreePartType();

    default boolean isRootNode() {
        return false;
    }

}
