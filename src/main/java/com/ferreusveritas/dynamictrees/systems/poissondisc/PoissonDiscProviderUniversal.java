package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoissonDiscProviderUniversal {

	// Dimension type works for vanilla but may need to be changed for modded support.
	Map<ResourceLocation, IPoissonDiscProvider> providerMap = new HashMap<>();

	protected IPoissonDiscProvider createCircleProvider(ServerWorld world) {
		BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(), world);
		IPoissonDiscProvider candidate = new PoissonDiscProvider(radiusCoordinator);
		PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world, candidate);
		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
	}

	public IPoissonDiscProvider getProvider(ServerWorld world) {
		return providerMap.computeIfAbsent(world.getDimensionKey().getLocation(), k -> createCircleProvider(world));
	}

	public List<PoissonDisc> getPoissonDiscs(ServerWorld world, ChunkPos chunkPos) {
		IPoissonDiscProvider provider = getProvider(world);
		return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
	}

	public void loadWorld(ServerWorld world) {
		// TODO Auto-generated method stub
	}

	public void unloadWorld(ServerWorld world) {
		providerMap.remove(world.getDimensionKey().getLocation());
	}

	public void setChunkPoissonData(ServerWorld world, ChunkPos chunkPos, byte[] circleData) {
		getProvider(world).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
	}

	public byte[] getChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
		return getProvider(world).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
	}

	public void unloadChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
		getProvider(world).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
	}

}
