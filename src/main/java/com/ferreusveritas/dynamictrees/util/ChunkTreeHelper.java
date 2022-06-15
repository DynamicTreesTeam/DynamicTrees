package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.PodBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.systems.nodemappers.CollectorNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static int removeOrphanedBranchNodes(World world, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null) {
            throw new NullPointerException("Null chunk position");
        }

        Set<BlockPos> found = new HashSet<>(); // This is used to track branches that are already proven
        final BlockBounds bounds = getEffectiveBlockBounds(world, chunkPos, radius);
        int orphansCleared = 0;

        for (BlockPos pos : bounds) {
            final BlockState state = world.getBlockState(pos);
            final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);

            if (!branchBlock.isPresent()) {
                continue; // No branch block found at this position.  Move on
            }

            // Test if the branch has a root node attached to it
            BlockPos rootPos = TreeHelper.findRootNode(world, pos);
            if (rootPos == BlockPos.ZERO) { // If the root position is the ORIGIN object it means that no root block was found
                // If the root node isn't found then all nodes are orphan.  Destroy the entire network.
                doTreeDestroy(world, branchBlock.get(), pos);
                orphansCleared++;
                continue;
            }

            // There is at least one root block in the network
            BlockState rootyState = world.getBlockState(rootPos);
            Optional<RootyBlock> rootyBlock = TreeHelper.getRootyOpt(rootyState);
            if (!rootyBlock.isPresent()) {
                continue; // This theoretically shouldn't ever happen
            }

            // Rooty block confirmed, build details about the trunk coming out of it
            Direction trunkDir = rootyBlock.get().getTrunkDirection(world, rootPos);
            BlockPos trunkPos = rootPos.relative(trunkDir);
            BlockState trunkState = world.getBlockState(trunkPos);
            Optional<BranchBlock> trunk = TreeHelper.getBranchOpt(trunkState);

            if (!trunk.isPresent()) {
                continue; // This theoretically shouldn't ever happen
            }

            // There's a trunk coming out of the rooty block, that's kinda expected.  But is it the only rooty block in the network?
            MapSignal signal = new MapSignal();
            signal.destroyLoopedNodes = false;
            trunk.get().analyse(trunkState, world, trunkPos, null, signal);
            if (signal.multiroot ||
                    signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network
                doTreeDestroy(world, branchBlock.get(), pos);
                orphansCleared++;
            } else { // Tree appears healthy with only a single attached root block
                trunk.get().analyse(trunkState, world, trunkPos, null, new MapSignal(new CollectorNode(found)));
            }
        }

        return orphansCleared;
    }

    public static int removeAllBranchesFromChunk(World world, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null) {
            throw new NullPointerException("Null chunk position");
        }

        final BlockBounds bounds = getEffectiveBlockBounds(world, chunkPos, radius);
        int treesCleared = 0;

        for (BlockPos pos : bounds) {
            BlockState state = world.getBlockState(pos);
            Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);
            if (branchBlock.isPresent()) {
                doTreeDestroy(world, branchBlock.get(), pos);
                treesCleared++;
            }
        }

        return treesCleared;
    }

    public static BlockBounds getEffectiveBlockBounds(World world, ChunkPos cPos, int radius) {
        Chunk chunk = world.getChunk(cPos.x, cPos.z);
        BlockBounds bounds = new BlockBounds(world, cPos);

        bounds.shrink(Direction.UP, (world.getHeight() - 1) - (getTopFilledSegment(chunk) + 16));
        for (Direction dir : Direction.Plane.HORIZONTAL.stream().collect(Collectors.toList())) {
            bounds.expand(dir, radius * CHUNK_WIDTH);
        }

        return bounds;
    }

    private static int getTopFilledSegment(final Chunk chunk) {
        final ChunkSection lastChunkSection = getLastSection(chunk);
        return lastChunkSection == null ? 0 : lastChunkSection.bottomBlockY();
    }

    @Nullable
    private static ChunkSection getLastSection(final Chunk chunk) {
        final ChunkSection[] sections = chunk.getSections();

        for (int i = sections.length - 1; i >= 0; i--) {
            if (sections[i] != null && !sections[i].isEmpty()) {
                return sections[i];
            }
        }

        return null;
    }

    private static void doTreeDestroy(World world, BranchBlock branchBlock, BlockPos pos) {
        BranchDestructionData destroyData = branchBlock.destroyBranchFromNode(world, pos, Direction.DOWN, true, null);
        destroyData.leavesDrops.clear(); // Prevent dropped seeds from planting themselves again
        FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0),
                FallingTreeEntity.DestroyType.ROOT); // Destroy the tree client side without fancy effects
        cleanupNeighbors(world, destroyData);
    }

    private static final byte NONE = (byte) 0;
    private static final byte TREE = (byte) 1;
    private static final byte SURR = (byte) 2;

    public static void cleanupNeighbors(World world, BranchDestructionData destroyData) {

        // Only run on the server since the block updates will come from the server anyway
        if (world.isClientSide) {
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
        outlineVoxmap.getAllNonZero(SURR).forEach(pos -> cleanupBlock(world, pos));
    }

    /**
     * Cleanup blocks that are attached(or setting on) various parts of the tree
     */
    public static void cleanupBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.AIR) { // This is the most likely case so bail early
            return;
        }

        Block block = state.getBlock();

        // Cleanup snow layers, hanging fruit(apples), trunk fruit(cocoa), and surface roots.
        if (block instanceof SnowBlock || block instanceof FruitBlock || block instanceof PodBlock ||
                block instanceof SurfaceRootBlock) {
            world.setBlock(pos, BlockStates.AIR, Constants.BlockFlags.BLOCK_UPDATE);
        } else if (block instanceof VineBlock) {
            // Cleanup vines
            cleanupVines(world, pos);
        }
    }

    /**
     * Cleanup vines starting the the top and moving down until a vine block is no longer found
     */
    public static void cleanupVines(World world, BlockPos pos) {
        BlockPos.Mutable mblock = pos.mutable(); // Mutable because ZOOM!
        while (world.getBlockState(mblock)
                .getBlock() instanceof VineBlock) {// BlockVine instance helps with modded vine types
            world.setBlock(mblock, BlockStates.AIR, Constants.BlockFlags.BLOCK_UPDATE);
            mblock.move(Direction.DOWN);
        }
    }

}