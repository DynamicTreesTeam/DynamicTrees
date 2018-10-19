package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import com.ferreusveritas.dynamictrees.worldgen.BiomeRadiusCoordinator;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

public class PoissonDiscProviderCreateEvent extends WorldEvent {
	
	private IPoissonDiscProvider poissonDiscProvider;
	private final BiomeRadiusCoordinator radiusCoordinator;
	
	public PoissonDiscProviderCreateEvent(World world, IPoissonDiscProvider poissonDiscProvider, BiomeRadiusCoordinator radiusCoordinator) {
		super(world);
		this.poissonDiscProvider = poissonDiscProvider;
		this.radiusCoordinator = radiusCoordinator;
	}
	
	public void setPoissonDiscProvider(IPoissonDiscProvider poissonDiscProvider) {
		this.poissonDiscProvider = poissonDiscProvider;
	}
	
	public IPoissonDiscProvider getPoissonDiscProvider() {
		return poissonDiscProvider;
	}
	
	public BiomeRadiusCoordinator getRadiusCoordinator() {
		return radiusCoordinator;
	}
	
}
