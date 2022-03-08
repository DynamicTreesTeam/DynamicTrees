package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalPoissonDiscProvider {

    private final Map<ResourceLocation, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

    protected PoissonDiscProvider createCircleProvider(WorldContext context) {
        final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(),
                context.dimensionName(), context.access());
        final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(context.access(),
                new LevelPoissonDiscProvider(radiusCoordinator).setSeed(context.seed()));
        MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
        return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
    }

    public PoissonDiscProvider getProvider(WorldContext context) {
            return this.providerMap.computeIfAbsent(context.dimensionName(), k -> createCircleProvider(context));
    }

    public List<PoissonDisc> getPoissonDiscs(WorldContext context, ChunkPos chunkPos) {
        final PoissonDiscProvider provider = getProvider(context);
        return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadWorld(ServerWorld world) {
        this.providerMap.remove(world.dimension().location());
    }

    public void setChunkPoissonData(WorldContext context, ChunkPos chunkPos, byte[] circleData) {
        this.getProvider(context).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
    }

    public byte[] getChunkPoissonData(WorldContext context, ChunkPos chunkPos) {
        return this.getProvider(context).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadChunkPoissonData(WorldContext context, ChunkPos chunkPos) {
        this.getProvider(context).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

}
