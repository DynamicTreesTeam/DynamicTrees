package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalPoissonDiscProvider {

    private final Map<ResourceLocation, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

    protected PoissonDiscProvider createCircleProvider(LevelContext levelContext) {
        final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(levelContext.dimensionName(), levelContext.accessor());
        final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(levelContext.accessor(),
                new LevelPoissonDiscProvider(radiusCoordinator).setSeed(levelContext.seed()));
        MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
        return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
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
