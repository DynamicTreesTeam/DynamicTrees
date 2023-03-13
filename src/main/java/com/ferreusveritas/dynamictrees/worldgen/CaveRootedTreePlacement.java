package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CaveRootedTreePlacement extends PlacementModifier {

    public static final CaveRootedTreePlacement INSTANCE = new CaveRootedTreePlacement();

    public static final PlacementModifierType<CaveRootedTreePlacement> TYPE = Registry.register(
            Registry.PLACEMENT_MODIFIERS,
            "cave_rooted_tree",
            () -> Codec.unit(() -> INSTANCE)
    );

    private CaveRootedTreePlacement() {
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, Random random, BlockPos pos) {
        WorldGenLevel level = context.getLevel();
        BiomeDatabase.Entry entry = BiomeDatabases.getDefault().getEntry(level.getLevel().getBiome(pos).value());
        if (!entry.hasCaveRootedEntry()) {
            return Stream.empty();
        }
        Supplier<Integer> randomHeight;
        if (entry.getCaveRootedEntry().shouldGenerateOnSurface()) {
            int maxDistToSurface = entry.getCaveRootedEntry().getMaxDistToSurface();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
            int minY = surfaceY - maxDistToSurface;
            randomHeight = () -> Mth.randomBetweenInclusive(random, minY, surfaceY);
        } else {
           randomHeight = () -> Mth.randomBetweenInclusive(random, context.getLevel().getMinBuildHeight(), context.getLevel().getMaxBuildHeight());
        }
        return DynamicTreeFeature.DISC_PROVIDER.getPoissonDiscs(LevelContext.create(level), new ChunkPos(pos)).stream()
                .map(disc -> new BlockPos(disc.x, randomHeight.get(), disc.z));
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
