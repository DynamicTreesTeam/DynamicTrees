package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalPoissonDiscProvider {

    private final Map<ResourceLocation, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

    protected PoissonDiscProvider createCircleProvider(ServerWorld world, IWorld iWorld) {
        final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(),
                world.dimension().location(), iWorld);
        final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world,
                new LevelPoissonDiscProvider(radiusCoordinator).setSeed(world.getSeed()));
        MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
        return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
    }

    public PoissonDiscProvider getProvider(ServerWorld world, IWorld iWorld) {
        return this.providerMap.computeIfAbsent(world.dimension().location(), k -> createCircleProvider(world, iWorld));
    }

    public List<PoissonDisc> getPoissonDiscs(ServerWorld world, IWorld iWorld, ChunkPos chunkPos) {
        final PoissonDiscProvider provider = getProvider(world, iWorld);
        return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadWorld(ServerWorld world) {
        this.providerMap.remove(world.dimension().location());
    }

    public void setChunkPoissonData(ServerWorld world, ChunkPos chunkPos, byte[] circleData) {
        this.getProvider(world, world).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
    }

    public byte[] getChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
        return this.getProvider(world, world).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

    public void unloadChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
        this.getProvider(world, world).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
    }

}
