package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.NullTreePart;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.systems.nodemapper.TwinkleNode;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;

public class TreeHelper {

    public static final TreePart NULL_TREE_PART = new NullTreePart();

    ///////////////////////////////////////////
    //CONVENIENCE METHODS
    ///////////////////////////////////////////

    /**
     * Convenience method to pulse a single growth cycle and age the cuboid volume. Used by growth potions, fertilizers
     * and the dendrocoil.
     */
    public static void growPulse(Level level, BlockPos rootPos) {
        BlockState rootyState = level.getBlockState(rootPos);
        RootyBlock dirt = TreeHelper.getRooty(rootyState);
        if (dirt != null) {
            dirt.updateTree(rootyState, level, rootPos, level.random, false);
            ageVolume(level, rootPos, 8, 32, 1, SafeChunkBounds.ANY);//blindly age a cuboid volume
        }
    }

    /**
     * Pulses an entire leafMap volume of blocks each with an age signal. Warning: CPU intensive and should be used
     * sparingly.
     *
     * @param level      The {@link LevelAccessor} instance.
     * @param leafMap    The voxel map of hydro values to use as an iterator.
     * @param iterations The number of times to age the volume.
     */
    public static void ageVolume(LevelAccessor level, SimpleVoxmap leafMap, int iterations, SafeChunkBounds safeBounds) {

        //The iterMap is the voxmap we will use as a discardable.  The leafMap must survive for snow
        SimpleVoxmap iterMap = leafMap != null ? new SimpleVoxmap(leafMap) : null;
        Iterable<BlockPos.MutableBlockPos> iterable = iterMap.getAllNonZero();

        for (int i = 0; i < iterations; i++) {
            for (BlockPos.MutableBlockPos iPos : iterable) {
                BlockState blockState = level.getBlockState(iPos);
                Block block = blockState.getBlock();
                if (block instanceof DynamicLeavesBlock) {//Special case for leaves
                    int prevHydro = leafMap.getVoxel(iPos);//The leafMap should contain accurate hydro data
                    int newHydro = ((Ageable) block).age(level, iPos, blockState, level.getRandom(), safeBounds);//Get new values from neighbors
                    if (newHydro == -1) {
                        //Leaf block died.  Take it out of the leafMap and iterMap
                        leafMap.setVoxel(iPos, (byte) 0);
                        iterMap.setVoxel(iPos, (byte) 0);
                    } else {
                        //Leaf did not die so the block is still leaves
                        if (prevHydro == newHydro) { //But it didn't change
                            iterMap.setVoxel(iPos, (byte) 0); //Stop iterating over it if it's not changing
                        } else {//Oh wait.. it did change
                            //Update both maps with this new hydro value
                            leafMap.setVoxel(iPos, (byte) newHydro);
                            iterMap.setVoxel(iPos, (byte) newHydro);
                            //Copy all the surrounding values from the leafMap to the iterMap since they now also have potential to change
                            for (Direction dir : Direction.values()) {
                                BlockPos dPos = iPos.relative(dir);
                                iterMap.setVoxel(dPos, leafMap.getVoxel(dPos));
                            }
                        }
                    }
                } else if (block instanceof Ageable) {//Treat as just a regular ageable block
                    ((Ageable) block).age(level, iPos, blockState, level.getRandom(), safeBounds);
                } else {//You're not supposed to be here
                    leafMap.setVoxel(iPos, (byte) 0);
                    iterMap.setVoxel(iPos, (byte) 0);
                }
            }
        }

    }

    /**
     * Pulses an entire cuboid volume of blocks each with an age signal. Warning: CPU intensive and should be used
     * sparingly
     *
     * @param level      The level
     * @param treePos    The position of the bottom most block of a trees trunk
     * @param halfWidth  The "radius" of the cuboid volume
     * @param height     The height of the cuboid volume
     * @param iterations The number of times to age the volume
     */
    public static void ageVolume(LevelAccessor level, BlockPos treePos, int halfWidth, int height, int iterations, SafeChunkBounds safeBounds) {
        //Slow and dirty iteration over a cuboid volume.  Try to avoid this by using a voxmap if you can
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(treePos.offset(new BlockPos(-halfWidth, 0, -halfWidth)), treePos.offset(new BlockPos(halfWidth, height, halfWidth)));
        for (int i = 0; i < iterations; i++) {
            for (BlockPos iPos : iterable) {
                BlockState blockState = level.getBlockState(iPos);
                Block block = blockState.getBlock();
                if (block instanceof Ageable) {
                    ((Ageable) block).age(level, iPos, blockState, level.getRandom(), safeBounds);//Treat as just a regular ageable block
                }
            }
        }

    }

    public static Optional<JoCode> getJoCode(Level level, BlockPos pos) {
        return getJoCode(level, pos, Direction.SOUTH);
    }

    public static Optional<JoCode> getJoCode(Level level, BlockPos pos, Direction direction) {
        if (pos == null) {
            return Optional.empty();
        }
        pos = dereferenceTrunkShell(level, pos);
        BlockPos rootPos = TreeHelper.findRootNode(level, pos);
        return rootPos != BlockPos.ZERO ? Optional.of(new JoCode(level, rootPos, direction)) : Optional.empty();
    }

    public static BlockPos dereferenceTrunkShell(Level level, BlockPos pos) {

        BlockState blockState = level.getBlockState(pos);

        if (blockState.getBlock() instanceof TrunkShellBlock) {
            TrunkShellBlock.ShellMuse muse = ((TrunkShellBlock) blockState.getBlock()).getMuse(level, blockState, pos);
            if (muse != null) {
                return muse.pos;
            }
        }

        return pos;
    }

    public static Species getCommonSpecies(Level level, BlockPos pos) {
        pos = dereferenceTrunkShell(level, pos);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BranchBlock) {
            BranchBlock branch = (BranchBlock) state.getBlock();
            return branch.getFamily().getCommonSpecies();
        }

        return Species.NULL_SPECIES;
    }

    /**
     * This is resource intensive.  Use only for interaction code. Only the root node can determine the exact species
     * and it has to be found by mapping the branch network.
     *
     * @param level The {@link Level} instance.
     * @param pos   The {@link BlockPos} to find the {@link Species} at.
     * @return The {@link Species}, or {@link Species#NULL_SPECIES} if one couldn't be found.
     */
    public static Species getExactSpecies(Level level, BlockPos pos) {
        BlockPos rootPos = findRootNode(level, pos);

        if (rootPos != BlockPos.ZERO) {
            BlockState rootyState = level.getBlockState(rootPos);
            return TreeHelper.getRooty(rootyState).getSpecies(rootyState, level, rootPos);
        }
        return Species.NULL_SPECIES;
    }

    /**
     * This is resource intensive.  Use only for interaction code. Only the root node can determine the exact species
     * and it has to be found by mapping the branch network.  Tries to find the exact species and if that fails tries to
     * find the common species.
     *
     * @param level
     * @param pos
     * @return
     */
    public static Species getBestGuessSpecies(Level level, BlockPos pos) {
        Species species = getExactSpecies(level, pos);
        return species == Species.NULL_SPECIES ? getCommonSpecies(level, pos) : species;
    }

    /**
     * Find the root node of a tree.
     *
     * @param level The level
     * @param pos   The position being analyzed
     * @return The position of the root node of the tree or BlockPos.ZERO if nothing was found.
     */
    public static BlockPos findRootNode(Level level, BlockPos pos) {

        pos = dereferenceTrunkShell(level, pos);
        BlockState state = level.getBlockState(pos);
        TreePart treePart = TreeHelper.getTreePart(level.getBlockState(pos));

        switch (treePart.getTreePartType()) {
            case BRANCH:
                MapSignal signal = treePart.analyse(state, level, pos, null, new MapSignal());// Analyze entire tree network to find root node
                if (signal.foundRoot) {
                    return signal.root;
                }
                break;
            case ROOT:
                return pos;
            default:
                return BlockPos.ZERO;
        }

        return BlockPos.ZERO;
    }

    /**
     * Sets a custom rooty block decay (what dirt it becomes when the tree is gone) algorithm for mods that have special
     * requirements.
     *
     * @param decay The {@link RootyBlockDecayer} implementation.
     */
    public static void setCustomRootBlockDecay(RootyBlockDecayer decay) {
        RootyBlock.rootyBlockDecayer = decay;
    }

    /**
     * Provided as a means for an implementation to chain the handlers.
     *
     * @return The currently defined {@link RootyBlockDecayer} handler.
     */
    public static RootyBlockDecayer getCustomRootBlockDecay() {
        return RootyBlock.rootyBlockDecayer;
    }

    /**
     * Convenience function that spawns particles all over the tree branches
     */
    public static void treeParticles(Level level, BlockPos rootPos, SimpleParticleType type, int num) {
        if (level.isClientSide) {
            startAnalysisFromRoot(level, rootPos, new MapSignal(new TwinkleNode(type, num)));
        }
    }

    public static void rootParticles(Level level, BlockPos rootPos, Direction offset, SimpleParticleType type, int num) {
        if (level.isClientSide) {
            if (level.isClientSide() && level.getBlockState(rootPos).getBlock() instanceof RootyBlock) {
                final BlockPos particlePos = rootPos.offset(offset.getNormal());
                DTClient.spawnParticles(level, type, particlePos.getX(), particlePos.getY(), particlePos.getZ(), num, level.getRandom());
            }
        }
    }

    /**
     * Convenience function that verifies an analysis is starting from the root node before commencing.
     *
     * @param level   The level
     * @param rootPos The position of the rootyBlock
     * @param signal  The signal carrying the inspectors
     * @return true if a root block was found.
     */
    public static boolean startAnalysisFromRoot(LevelAccessor level, BlockPos rootPos, MapSignal signal) {
        RootyBlock dirt = TreeHelper.getRooty(level.getBlockState(rootPos));
        if (dirt != null) {
            dirt.startAnalysis(level, rootPos, signal);
            return true;
        }
        return false;
    }

    /**
     * Destroys a part of the tree from the specified {@code cutPos}. Destruction is handled fully by this method,
     * except for drops which are returned to be handled by the caller using the {@code dropConsumer} parameter.
     * <p>
     * This may be used for mod compatibility with machines that cut down trees automatically, for example the Create
     * saw. This method was based on how the compatibility with Create worked, making it easier for them and others
     * to provide integration.
     *
     * @param dropConsumer called for all drops with the block position at which they should drop and the stack.
     *                     For logs and sticks this will be the cut position, while for leaves this will be the
     *                     position of the respective leave block that created this drop.
     */
    public static void destroyTree(Level level, BlockPos cutPos, @Nullable Player player, BiConsumer<BlockPos, ItemStack> dropConsumer) {
        BlockPos startPos = dereferenceTrunkShell(level, cutPos);
        BranchBlock cutBlock = getBranch(level.getBlockState(cutPos));

        // Fire event for break sound and particles
        level.levelEvent(null, 2001, cutPos, Block.getId(level.getBlockState(cutPos)));

        BranchDestructionData destructionData = cutBlock.destroyBranchFromNode(level, startPos, Direction.DOWN, false, player);

        // Allow drop consumer callback to handle drops
        destructionData.leavesDrops.forEach(stackData -> dropConsumer.accept(stackData.pos, stackData.stack));
        destructionData.species.getBranchesDrops(level, destructionData.woodVolume).forEach(stack -> dropConsumer.accept(startPos, stack));
    }

    //Treeparts

    public static boolean isTreePart(Block block) {
        return block instanceof TreePart;
    }

    public static boolean isTreePart(BlockState state) {
        return isTreePart(state.getBlock());
    }

    public static boolean isTreePart(LevelAccessor level, BlockPos pos) {
        return isTreePart(level.getBlockState(pos).getBlock());
    }

    public static TreePart getTreePart(Block block) {
        return isTreePart(block) ? (TreePart) block : NULL_TREE_PART;
    }

    public static TreePart getTreePart(BlockState state) {
        return getTreePart(state.getBlock());
    }


    //Branches

    public static boolean isBranch(Block block) {
        return block instanceof BranchBlock;//Oh shuddap you java purists.. this is minecraft!
    }

    public static boolean isBranch(@Nullable final BlockState state) {
        return state != null && isBranch(state.getBlock());
    }

    @Nullable
    public static BranchBlock getBranch(Block block) {
        return isBranch(block) ? (BranchBlock) block : null;
    }

    @Nullable
    public static BranchBlock getBranch(TreePart treePart) {
        return treePart instanceof BranchBlock ? (BranchBlock) treePart : null;
    }

    @Nullable
    public static BranchBlock getBranch(BlockState state) {
        return getBranch(state.getBlock());
    }

    public static int getRadius(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return getTreePart(state).getRadius(state);
    }

    public static Optional<BranchBlock> getBranchOpt(Block block) {
        return isBranch(block) ? Optional.of((BranchBlock) block) : Optional.empty();
    }

    public static Optional<BranchBlock> getBranchOpt(BlockState state) {
        final Block block = state.getBlock();
        return isBranch(block) ? Optional.of((BranchBlock) block) : Optional.empty();
    }

    public static Optional<BranchBlock> getBranchOpt(TreePart treePart) {
        return treePart instanceof BranchBlock ? Optional.of((BranchBlock) treePart) : Optional.empty();
    }

    public static Optional<RootyBlock> getRootyOpt(BlockState state) {
        Block block = state.getBlock();
        return isRooty(block) ? Optional.of((RootyBlock) block) : Optional.empty();
    }

    //Leaves

    public static boolean isLeaves(Block block) {
        return block instanceof DynamicLeavesBlock;
    }

    public static boolean isLeaves(BlockState state) {
        return isLeaves(state.getBlock());
    }

    @Nullable
    public static DynamicLeavesBlock getLeaves(Block block) {
        return isLeaves(block) ? (DynamicLeavesBlock) block : null;
    }

    @Nullable
    public static DynamicLeavesBlock getLeaves(TreePart treePart) {
        return treePart instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) treePart : null;
    }

    @Nullable
    public static DynamicLeavesBlock getLeaves(BlockState state) {
        return getLeaves(state.getBlock());
    }

    //Rooty

    public static boolean isRooty(Block block) {
        return block instanceof RootyBlock;
    }

    public static boolean isRooty(BlockState state) {
        return isRooty(state.getBlock());
    }

    @Nullable
    public static RootyBlock getRooty(Block block) {
        return isRooty(block) ? (RootyBlock) block : null;
    }

    @Nullable
    public static RootyBlock getRooty(TreePart treePart) {
        return treePart instanceof RootyBlock ? (RootyBlock) treePart : null;
    }

    @Nullable
    public static RootyBlock getRooty(BlockState state) {
        return getRooty(state.getBlock());
    }

}
