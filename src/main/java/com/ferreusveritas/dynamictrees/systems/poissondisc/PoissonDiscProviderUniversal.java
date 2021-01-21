//package com.ferreusveritas.dynamictrees.systems.poissondisc;
//
//import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
//import com.ferreusveritas.dynamictrees.event.PoissonDiscProviderCreateEvent;
//import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
//import net.minecraft.world.Dimension;
//import net.minecraft.world.World;
//import net.minecraftforge.common.MinecraftForge;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class PoissonDiscProviderUniversal {
//
//	Map<Dimension, IPoissonDiscProvider> providerMap = new HashMap<>();
//
//	protected IPoissonDiscProvider createCircleProvider(World world) {
//		BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(), world);
//		IPoissonDiscProvider candidate = new PoissonDiscProvider(radiusCoordinator);
//		PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world, candidate);
//		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
//		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
//	}
//
//	public IPoissonDiscProvider getProvider(World world) {
//		return providerMap.computeIfAbsent(world.getDimension(), d -> createCircleProvider(world));
//	}
//
//	public List<PoissonDisc> getPoissonDiscs(World world, int chunkX, int chunkY, int chunkZ) {
//		IPoissonDiscProvider provider = getProvider(world);
//		return provider.getPoissonDiscs(chunkX, chunkY, chunkZ);
//	}
//
//	public void loadWorld(World world) {
//		// TODO Auto-generated method stub
//	}
//
//	public void unloadWorld(World world) {
//		providerMap.remove(world.getDimension());
//	}
//
//	public void setChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ, byte[] circleData) {
//		getProvider(world).setChunkPoissonData(chunkX, chunkY, chunkZ, circleData);
//	}
//
//	public byte[] getChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
//		return getProvider(world).getChunkPoissonData(chunkX, chunkY, chunkZ);
//	}
//
//	public void unloadChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ) {
//		getProvider(world).unloadChunkPoissonData(chunkX, chunkY, chunkZ);
//	}
//
//}
