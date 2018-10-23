package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
import com.ferreusveritas.dynamictrees.util.PoissonDisc;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class PoissonDiscProviderUniversal implements IPoissonDiscProvider {
	
	Map<Integer, IPoissonDiscProvider> providerMap = new HashMap<>();
	
	public final BiomeRadiusCoordinator radiusCoordinator; //Finds radius for coordinates
	
	public PoissonDiscProviderUniversal(TreeGenerator treeGenerator) {
		radiusCoordinator = new BiomeRadiusCoordinator(treeGenerator);
	}
	
	protected IPoissonDiscProvider createCircleProvider(World world) {
		IPoissonDiscProvider candidate = new PoissonDiscProviderWorld(radiusCoordinator);
		PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world, candidate, radiusCoordinator);
		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
	}
	
	public IPoissonDiscProvider getProvider(World world) {
		return providerMap.computeIfAbsent(world.provider.getDimension(), d -> createCircleProvider(world));
	}
	
	@Override
	public List<PoissonDisc> getPoissonDiscs(World world, int chunkX, int chunkY, int chunkZ) {
		IPoissonDiscProvider provider = getProvider(world);
		return provider.getPoissonDiscs(world, chunkX, chunkY, chunkZ);
	}
	
	public void loadWorld(World world) {
		// TODO Auto-generated method stub
	}
	
	public void unloadWorld(World world) {
		providerMap.remove(world.provider.getDimension());
	}
	
	public void setChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ, byte[] circleData) {
		getProvider(world).setChunkPoissonData(world, chunkX, chunkY, chunkZ, circleData);
	}

	public byte[] getChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
		return getProvider(world).getChunkPoissonData(world, chunkX, chunkY, chunkZ);
	}
	
	public void unloadChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
		getProvider(world).unloadChunkPoissonData(world, chunkX, chunkY, chunkZ);
	}
	
}
