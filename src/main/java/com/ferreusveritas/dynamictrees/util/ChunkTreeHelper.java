package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.systems.nodemapper.CollectorNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ferreusveritas
 */
public class ChunkTreeHelper {

    private static final int CHUNK_WIDTH = 16;

    /**
     * Removes floating little bits of tree that have somehow lost connection with their parent root system.
     *
     * @param chunkPos the chunk position where the effect is intended
     * @param radius   radius of effect in chunk width units
     */
    public static int removeOrphanedBranchNodes(Level level, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null) {
            throw new NullPointerException("Null chunk position");
        }

        Set<BlockPos> found = new HashSet<>(); // This is used to track branches that are already proven
        final BlockBounds bounds = getEffectiveBlockBounds(level, chunkPos, radius);
        int orphansCleared = 0;

        for (BlockPos pos : bounds) {
            final BlockState state = level.getBlockState(pos);
            final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);

            if (branchBlock.isEmpty()) {
                continue; // No branch block found at this position.  Move on
            }

            // Test if the branch has a root node attached to it
            BlockPos rootPos = TreeHelper.findRootNode(level, pos);
            if (rootPos == BlockPos.ZERO) { // If the root position is the ORIGIN object it means that no root block was found
                // If the root node isn't found then all nodes are orphan.  Destroy the entire network.
                doTreeDestroy(level, branchBlock.get(), pos);
                orphansCleared++;
                continue;
            }

            // There is at least one root block in the network
            BlockState rootyState = level.getBlockState(rootPos);
            Optional<RootyBlock> rootyBlock = TreeHelper.getRootyOpt(rootyState);
            if (rootyBlock.isEmpty()) {
                continue; // This theoretically shouldn't ever happen
            }

            // Rooty block confirmed, build details about the trunk coming out of it
            Direction trunkDir = rootyBlock.get().getTrunkDirection(level, rootPos);
            BlockPos trunkPos = rootPos.relative(trunkDir);
            BlockState trunkState = level.getBlockState(trunkPos);
            Optional<BranchBlock> trunk = TreeHelper.getBranchOpt(trunkState);

            if (trunk.isEmpty()) {
                continue; // This theoretically shouldn't ever happen
            }

            // There's a trunk coming out of the rooty block, that's kinda expected.  But is it the only rooty block in the network?
            MapSignal signal = new MapSignal();
            signal.destroyLoopedNodes = false;
            trunk.get().analyse(trunkState, level, trunkPos, null, signal);
            if (signal.multiroot || signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network
                doTreeDestroy(level, branchBlock.get(), pos);
                orphansCleared++;
            } else { // Tree appears healthy with only a single attached root block
                trunk.get().analyse(trunkState, level, trunkPos, null, new MapSignal(new CollectorNode(found)));
            }
        }

        return orphansCleared;
    }

    public static int removeAllBranchesFromChunk(Level level, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null) {
            throw new NullPointerException("Null chunk position");
        }

        final BlockBounds bounds = getEffectiveBlockBounds(level, chunkPos, radius);
        final AtomicInteger treesCleared = new AtomicInteger();

        for (BlockPos pos : bounds) {
            BlockState state = level.getBlockState(pos);
            TreeHelper.getBranchOpt(state).ifPresent(branchBlock -> {
                doTreeDestroy(level, branchBlock, pos);
                treesCleared.getAndIncrement();
            });
        }

        return treesCleared.get();
    }

    public static BlockBounds getEffectiveBlockBounds(Level level, ChunkPos chunkPos, int radius) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        BlockBounds bounds = new BlockBounds(level, chunkPos);

        bounds.shrink(Direction.UP, (level.getHeight() - 1) - (getTopFilledSegment(chunk) + 16));
        for (Direction dir : Direction.Plane.HORIZONTAL.stream().toList()) {
            bounds.expand(dir, radius * CHUNK_WIDTH);
        }

        return bounds;
    }

    @SuppressWarnings("removal")
    private static int getTopFilledSegment(final LevelChunk chunk) {
        return chunk.getHighestSectionPosition();
    }

    private static void doTreeDestroy(Level level, BranchBlock branchBlock, BlockPos pos) {
        BranchDestructionData destroyData = branchBlock.destroyBranchFromNode(level, pos, Direction.DOWN, true, null);
        destroyData.leavesDrops.clear(); // Prevent dropped seeds from planting themselves again
        FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0),
                FallingTreeEntity.DestroyType.ROOT); // Destroy the tree client side without fancy effects
        cleanupNeighbors(level, destroyData);
    }

    private static final byte NONE = (byte) 0;
    private static final byte TREE = (byte) 1;
    private static final byte SURR = (byte) 2;

    public static void cleanupNeighbors(Level level, BranchDestructionData destroyData) {

        // Only run on the server since the block updates will come from the server anyway
        if (level.isClientSide) {
            return;
        }

        // Get the bounds of the tree, all leaves and branches but not the rooty block
        BlockBounds treeBounds = new BlockBounds(destroyData.cutPos);
        destroyData.getPositions(BranchDestructionData.PosType.LEAVES, true).forEach(treeBounds::union);
        destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, true).forEach(treeBounds::union);
        treeBounds.expand(1); // Expand by one to contain the 3d "outline" of the voxels

        // Mark voxels for leaves or branch blocks
        SimpleVoxmap treeVoxmap = new SimpleVoxmap(treeBounds);
        destroyData.getPositions(BranchDestructionData.PosType.LEAVES, true)
                .forEach(pos -> treeVoxmap.setVoxel(pos, TREE));
        destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, true)
                .forEach(pos -> treeVoxmap.setVoxel(pos, TREE));

        // Set voxels in the outline map for any adjacent voxels from the source tree map
        SimpleVoxmap outlineVoxmap = new SimpleVoxmap(treeVoxmap);
        treeVoxmap.getAllNonZero(TREE).forEach(pos -> {
            for (Direction dir : Direction.values()) {
                outlineVoxmap.setVoxel(pos.move(dir.getNormal()), SURR);
            }
        });

        // Clear out the original positions of the leaves and branch blocks since they've already been deleted
        treeVoxmap.getAllNonZero(TREE).forEach(pos -> outlineVoxmap.setVoxel(pos, NONE));

        // Finally use this map for cleaning up marked block positions
        outlineVoxmap.getAllNonZero(SURR).forEach(pos -> cleanupBlock(level, pos));
    }

    /**
     * Cleanup blocks that are attached(or setting on) various parts of the tree
     */
    public static void cleanupBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.AIR) { // This is the most likely case so bail early
            return;
        }

        Block block = state.getBlock();

        // Cleanup snow layers, hanging fruit(apples), trunk fruit(cocoa), and surface roots.
        if (block instanceof SnowLayerBlock || block instanceof FruitBlock || block instanceof PodBlock ||
                block instanceof SurfaceRootBlock) {
            level.setBlock(pos, BlockStates.AIR, Block.UPDATE_CLIENTS);
        } else if (block instanceof VineBlock) {
            // Cleanup vines
            cleanupVines(level, pos);
        }
    }

    /**
     * Cleanup vines starting the the top and moving down until a vine block is no longer found
     */
    public static void cleanupVines(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mblock = pos.mutable(); // Mutable because ZOOM!
        while (level.getBlockState(mblock)
                .getBlock() instanceof VineBlock) {// BlockVine instance helps with modded vine types
            level.setBlock(mblock, BlockStates.AIR, Block.UPDATE_CLIENTS);
            mblock.move(Direction.DOWN);
        }
    }

}