package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PoissonDiscProviderUniversal {

	// Dimension type works for vanilla but may need to be changed for modded support.
	Map<DimensionType, IPoissonDiscProvider> providerMap = new HashMap<>();

	protected IPoissonDiscProvider createCircleProvider(IWorld dimType) {
		BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(), dimType);
		IPoissonDiscProvider candidate = new PoissonDiscProvider(radiusCoordinator);
		PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(dimType, candidate);
		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
	}

	public IPoissonDiscProvider getProvider(IWorld world) {
		return providerMap.computeIfAbsent(world.getDimensionType(), d -> createCircleProvider(world));
	}

	public List<PoissonDisc> getPoissonDiscs(IWorld world, int chunkX, int chunkY, int chunkZ) {
		IPoissonDiscProvider provider = getProvider(world);
		return provider.getPoissonDiscs(chunkX, chunkY, chunkZ);
	}

	public void loadWorld(IWorld world) {
		// TODO Auto-generated method stub
	}

	public void unloadWorld(IWorld world) {
		providerMap.remove(world.getDimensionType());
	}

	public void setChunkPoissonData(IWorld world, int chunkX, int chunkY, int chunkZ, byte[] circleData) {
		getProvider(world).setChunkPoissonData(chunkX, chunkY, chunkZ, circleData);
	}

	public byte[] getChunkPoissonData(IWorld world, int chunkX, int chunkY, int chunkZ) {
		return getProvider(world).getChunkPoissonData(chunkX, chunkY, chunkZ);
	}

	public void unloadChunkPoissonData(IWorld world, int chunkX, int chunkY, int chunkZ) {
		getProvider(world).unloadChunkPoissonData(chunkX, chunkY, chunkZ);
	}

}
