package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class CaveRootedTreePlacement extends PlacementModifier {
    public static final CaveRootedTreePlacement INSTANCE = new CaveRootedTreePlacement();
    public static final Codec<CaveRootedTreePlacement> CODEC = Codec.unit(() -> INSTANCE);

    private CaveRootedTreePlacement() {
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        return DynamicTreeFeature.DISC_PROVIDER.getPoissonDiscs(LevelContext.create(context.getLevel()), new ChunkPos(pos))
                .stream()
                .map(disc -> new BlockPos(disc.x, 0, disc.z));
    }

    @Override
    public PlacementModifierType<?> type() {
        return DTRegistries.CAVE_ROOTED_TREE_PLACEMENT_MODIFIER_TYPE.get();
    }
}
