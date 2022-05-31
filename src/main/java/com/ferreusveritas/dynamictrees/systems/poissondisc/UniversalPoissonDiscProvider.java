package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalPoissonDiscProvider {

    private final Map<ResourceLocation, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

    protected PoissonDiscProvider createCircleProvider(ServerLevel world, LevelAccessor iWorld) {
        final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(),
                world.dimension().location(), iWorld);
        final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world,
                new LevelPoissonDiscProvider(radiusCoordinator).setSeed(world.getSeed()));
        MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
        return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
    }

    public PoissonDiscProvider getProvider(ServerLevel world, LevelAccessor iWorld) {
        return this.providerMap.computeIfAbsent(world.dimension().location(), k -> createCircleProvider(world, iWorld));
    }

    public List<PoissonDisc> getPoissonDiscs(ServerLevel world, LevelAccessor iWorld, ChunkPos chunkPos) {
        final PoissonDiscProvider provider = getProvider(world, iWorld);
        return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadWorld(ServerLevel world) {
        this.providerMap.remove(world.dimension().location());
    }

    public void setChunkPoissonData(ServerLevel world, ChunkPos chunkPos, byte[] circleData) {
        this.getProvider(world, world).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
    }

    public byte[] getChunkPoissonData(ServerLevel world, ChunkPos chunkPos) {
        return this.getProvider(world, world).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadChunkPoissonData(ServerLevel world, ChunkPos chunkPos) {
        this.getProvider(world, world).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

}
