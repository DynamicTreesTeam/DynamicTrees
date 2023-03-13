package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;
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
        return DynamicTreeFeature.DISC_PROVIDER.getPoissonDiscs(LevelContext.create(context.getLevel()), new ChunkPos(pos))
                .stream()
                .map(disc -> new BlockPos(disc.x, 0, disc.z));
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
