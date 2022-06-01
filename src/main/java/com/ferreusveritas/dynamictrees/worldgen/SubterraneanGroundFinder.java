package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles finding a suitable ground block on which a tree can generate in subterranean locations, such as the Nether. 
 */
public class SubterraneanGroundFinder implements GroundFinder {

    private static final List<BlockPos> NO_LAYERS = Collections.singletonList(BlockPos.ZERO);

    protected boolean isReplaceable(final IWorld world, final BlockPos pos) {
        return (world.isEmptyBlock(pos) || !world.getBlockState(pos).getMaterial().blocksMotion() || world.getBlockState(pos).getBlock() instanceof DynamicSaplingBlock) && !world.getBlockState(pos).getMaterial().isLiquid();
    }

    protected boolean inRange(final BlockPos pos, final int minY, final int maxY) {
        return pos.getY() >= minY && pos.getY() <= maxY;
    }

    protected int getTopY(final IWorld world, final BlockPos pos) {
        return world.getChunk(pos).getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    }

    protected ArrayList<Integer> findSubterraneanLayerHeights(final IWorld world, final BlockPos start) {
        final int maxY = this.getTopY(world, start);

        final BlockPos.Mutable pos = new BlockPos.Mutable(start.getX(), 0, start.getZ());
        final ArrayList<Integer> layers = new ArrayList<>();

        while (this.inRange(pos, 0, maxY)) {
            while (!isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) {
                pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit air
            }
            while (isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) {
                pos.move(Direction.DOWN); // Move down 1 block at a time until we hit not-air
            }
            if (isReplaceable(world, pos.above(6))) { // If there is air 6 blocks above it is likely that the layer is not too cramped
                layers.add(pos.getY()); // Record this position
            }
            pos.move(Direction.UP, 8); // Move up 8 blocks
            while (isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) {
                pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit ground
            }
        }

        // Discard the last result as it's just the top of the biome(bedrock for nether)
        if (layers.size() > 0) {
            layers.remove(layers.size() - 1);
        }

        return layers;
    }

    @Override
    public List<BlockPos> findGround(IWorld world, BlockPos start) {
        final ArrayList<Integer> layers = findSubterraneanLayerHeights(world, start);
        if (layers.size() < 1) {
            return NO_LAYERS;
        }
        List<BlockPos> positions = new LinkedList<>();
        for (int y : layers) {
            positions.add(new BlockPos(start.getX(), y, start.getZ()));
        }

        return positions;
    }

}
