package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles finding a suitable ground block on which a tree can generate in subterranean locations, such as the Nether. 
 */
public class SubterraneanGroundFinder implements GroundFinder {

    private static final List<BlockPos> NO_LAYERS = Collections.singletonList(BlockPos.ZERO);

    protected boolean isReplaceable(final LevelAccessor level, final BlockPos pos) {
        return (level.isEmptyBlock(pos) || level.getBlockState(pos).is(BlockTags.REPLACEABLE_BY_TREES)) && level.getBlockState(pos).getFluidState().isEmpty();
    }

    protected int getTopY(final LevelAccessor level, final BlockPos pos) {
        return level.getChunk(pos).getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    }

    protected ArrayList<Integer> findSubterraneanLayerHeights(final LevelAccessor level, final BlockPos start) {
        final int maxY = this.getTopY(level, start);
        final int minY = level.getMinBuildHeight();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start.getX(), minY, start.getZ());
        final ArrayList<Integer> layers = new ArrayList<>();

        while (CoordUtils.inRange(pos, minY, maxY)) {
            while (!isReplaceable(level, pos) && CoordUtils.inRange(pos, minY, maxY)) {
                pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit air
            }
            while (isReplaceable(level, pos) && CoordUtils.inRange(pos, minY, maxY)) {
                pos.move(Direction.DOWN); // Move down 1 block at a time until we hit not-air
            }
            if (isReplaceable(level, pos.above(6))) { // If there is air 6 blocks above it is likely that the layer is not too cramped
                layers.add(pos.getY()); // Record this position
            }
            pos.move(Direction.UP, 8); // Move up 8 blocks
            while (isReplaceable(level, pos) && CoordUtils.inRange(pos, minY, maxY)) {
                pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit ground
            }
        }

        // Discard the last result as it's just the top of the biome(bedrock for nether)
        if (!layers.isEmpty()) {
            layers.removeLast();
        }

        return layers;
    }

    @Override
    public List<BlockPos> findGround(LevelAccessor level, BlockPos start, @Nullable Heightmap.Types heightmap) {
        final ArrayList<Integer> layers = findSubterraneanLayerHeights(level, start);
        if (layers.isEmpty()) {
            return NO_LAYERS;
        }
        List<BlockPos> positions = new LinkedList<>();
        for (int y : layers) {
            BlockPos pos = new BlockPos(start.getX(), y, start.getZ());
            //We only want positions for underground biomes and underground dimensions
            if (level.dimensionType().hasCeiling() || level.getBiome(pos).is(TagKey.create(Registries.BIOME, Identifier.parse("c:is_underground"))))
                positions.add(pos);
        }

        return positions;
    }

}
