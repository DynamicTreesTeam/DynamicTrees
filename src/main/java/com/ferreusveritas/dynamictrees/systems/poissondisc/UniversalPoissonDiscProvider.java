package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversalPoissonDiscProvider {

	private final Map<ResourceLocation, IPoissonDiscProvider> providerMap = new HashMap<>();

	protected IPoissonDiscProvider createCircleProvider(ServerWorld world, IWorld iWorld) {
		final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(), world.dimension().location(), iWorld);
		final PoissonDiscProvider candidate = new PoissonDiscProvider(radiusCoordinator);
		candidate.setSeed(world.getSeed());
		final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world, candidate);
		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
	}

	public IPoissonDiscProvider getProvider(ServerWorld world, IWorld iWorld) {
		return this.providerMap.computeIfAbsent(world.dimension().location(), k -> createCircleProvider(world, iWorld));
	}

	public List<PoissonDisc> getPoissonDiscs(ServerWorld world, IWorld iWorld, ChunkPos chunkPos) {
		final IPoissonDiscProvider provider = getProvider(world, iWorld);
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
