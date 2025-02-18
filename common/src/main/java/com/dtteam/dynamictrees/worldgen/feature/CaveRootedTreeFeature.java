package com.dtteam.dynamictrees.worldgen.feature;

import com.dtteam.dynamictrees.api.worldgen.GroundFinder;
import com.dtteam.dynamictrees.systems.poissondisc.PoissonDisc;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;
import java.util.Optional;

public class CaveRootedTreeFeature extends DynamicTreeFeature {

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ResourceLocation dimensionName = level.getLevel().dimension().location();

        // Do not generate if the current dimension is blacklisted.
        if (BiomeDatabases.isBlacklisted(dimensionName)) {
            return false;
        }

        BlockPos originPos = context.origin();
        ChunkPos chunkPos = level.getChunk(originPos).getPos();
        LevelContext levelContext = LevelContext.create(level);

        PoissonDisc disc = getDisc(levelContext, chunkPos, originPos).orElse(null);
        if (disc == null) {
            return false;
        }

        List<BlockPos> groundPositions = GroundFinder.getGroundFinder(level.getLevel()).findGround(level, originPos, null);
        if (groundPositions.isEmpty()) {
            return false;
        }

        BiomeDatabase.Entry biomeEntry = BiomeDatabases.getDefault().getEntry(level.getLevel().getBiome(originPos));
        if (!biomeEntry.hasCaveRootedData())
            return false;

        BiomeDatabase.CaveRootedData caveRootedData = biomeEntry.getCaveRootedData();
        BlockPos groundPos = caveRootedData.shouldGenerateOnSurface() ? groundPositions.get(groundPositions.size() - 1)
                : getNextGroundPos(originPos, groundPositions).orElse(null);
        if (groundPos == null || groundPos.getY() - originPos.getY() > caveRootedData.getMaxDistToSurface()) {
            return false;
        }

        GeneratorResult result = this.generateTree(levelContext, biomeEntry, disc, originPos, groundPos);
        return result == GeneratorResult.GENERATED;
    }

    private Optional<PoissonDisc> getDisc(LevelContext levelContext, ChunkPos chunkPos, BlockPos originPos) {
        return DISC_PROVIDER.getPoissonDiscs(levelContext, chunkPos).stream()
                .filter(disc -> disc.x == originPos.getX() && disc.z == originPos.getZ())
                .findFirst();
    }

    private Optional<BlockPos> getNextGroundPos(BlockPos originPos, List<BlockPos> groundPositions) {
        for (BlockPos groundPos: groundPositions) {
            if (groundPos.getY() > originPos.getY()) {
                return Optional.of(groundPos);
            }
        }
        return Optional.empty();
    }

}
