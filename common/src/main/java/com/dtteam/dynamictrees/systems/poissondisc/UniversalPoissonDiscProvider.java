package com.dtteam.dynamictrees.systems.poissondisc;

import com.dtteam.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.util.LevelContext;
import com.dtteam.dynamictrees.worldgen.BiomeRadiusCoordinator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalPoissonDiscProvider {

    private final Map<ResourceLocation, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

    protected PoissonDiscProvider createCircleProvider(LevelContext levelContext) {
        final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(levelContext.dimensionName(), levelContext.accessor());
        return Services.EVENT.postPoissonDiscProviderCreateEvent( //This event allows the disc provider to be modified.
                levelContext.accessor(),
                new LevelPoissonDiscProvider(radiusCoordinator).setSeed(levelContext.seed()));
    }

    public PoissonDiscProvider getProvider(LevelContext levelContext) {
        return this.providerMap.computeIfAbsent(levelContext.dimensionName(), k -> createCircleProvider(levelContext));
    }

    public List<PoissonDisc> getPoissonDiscs(LevelContext levelContext, ChunkPos chunkPos) {
        final PoissonDiscProvider provider = getProvider(levelContext);
        return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadWorld(ServerLevel level) {
        this.providerMap.remove(level.dimension().location());
    }

    public void setChunkPoissonData(LevelContext levelContext, ChunkPos chunkPos, byte[] circleData) {
        this.getProvider(levelContext).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
    }

    public byte[] getChunkPoissonData(LevelContext levelContext, ChunkPos chunkPos) {
        return this.getProvider(levelContext).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadChunkPoissonData(LevelContext levelContext, ChunkPos chunkPos) {
        this.getProvider(levelContext).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

}
