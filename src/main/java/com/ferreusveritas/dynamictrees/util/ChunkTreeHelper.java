package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author ferreusveritas
 */
public class ChunkTreeHelper {

    public static void removeOrphanedBranchNodes(World world, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null)
            return;

        final Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
        final BlockBounds bounds = getBounds(world, chunk, radius);

        for (BlockPos pos: bounds) {
            final BlockState state = world.getBlockState(pos);
            final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);

            if (branchBlock.isPresent()) {
                BlockPos rootPos = TreeHelper.findRootNode(world, pos);

                if (rootPos == BlockPos.ZERO) { // If the root position is the ORIGIN object it means that no root block was found.
                    // If the root node isn't found then all nodes are orphan.  Destroy the entire network.
                    BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, Direction.DOWN, true, null);
                    FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
                } else {
                    // There is at least one root block in the network.
                    final BlockState rootyState = world.getBlockState(rootPos);
                    final Optional<RootyBlock> rootyBlock = TreeHelper.getRootyOpt(rootyState);

                    if (rootyBlock.isPresent()) { // Rooty block confirmed
                        final Direction trunkDir = rootyBlock.get().getTrunkDirection(world, rootPos);
                        final BlockPos trunkPos = rootPos.relative(trunkDir);
                        final BlockState trunkState = world.getBlockState(trunkPos);
                        final Optional<BranchBlock> trunk = TreeHelper.getBranchOpt(trunkState);

                        if (trunk.isPresent()) { // There's a trunk coming out of the rooty block, that's kinda expected
                            final MapSignal signal = new MapSignal();
                            signal.destroyLoopedNodes = false;
                            trunk.get().analyse(trunkState, world, trunkPos, null, signal);

                            if (signal.multiroot || signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network.
                                BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, Direction.DOWN, true, null);
                                FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT); // Destroy the tree client side without fancy effects.
                            }
                        }
                    }
                }
            }
        }

    }

    public static void removeAllBranchesFromChunk(World world, @Nullable ChunkPos chunkPos, int radius) {
        if (chunkPos == null)
            return;

        final Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
        final BlockBounds bounds = getBounds(world, chunk, radius);

        for (BlockPos pos: bounds) {
            BlockState state = world.getBlockState(pos);
            Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);
            if (branchBlock.isPresent()) {
                BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, Direction.DOWN, true, null);
                FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);// Destroy the tree client side without fancy effects
            }
        }

    }

    private static BlockBounds getBounds(final World world, final Chunk chunk, int radius) {
        final BlockBounds bounds = new BlockBounds(chunk.getPos());

        bounds.shrink(Direction.UP, world.getHeight() - 1 - (getTopFilledSegment(chunk) + 16));
        for (Direction dir : CoordUtils.HORIZONTALS) {
            bounds.expand(dir, radius * 16);
        }

        return bounds;
    }

    private static int getTopFilledSegment (final Chunk chunk) {
        final ChunkSection lastChunkSection = getLastSection(chunk);
        return lastChunkSection == null ? 0 : lastChunkSection.bottomBlockY();
    }

    @Nullable
    private static ChunkSection getLastSection (final Chunk chunk) {
        final ChunkSection[] sections = chunk.getSections();

        for (int i = sections.length - 1; i >= 0; i--) {
            if (sections[i] != null && !sections[i].isEmpty())
                return sections[i];
        }

        return null;
    }

}