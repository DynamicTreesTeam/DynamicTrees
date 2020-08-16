package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.worldgen.IPoissonDiscProvider;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

public class PoissonDiscProviderCreateEvent extends WorldEvent {
	
	private IPoissonDiscProvider poissonDiscProvider;
	
	public PoissonDiscProviderCreateEvent(World world, IPoissonDiscProvider poissonDiscProvider) {
		super(world);
		this.poissonDiscProvider = poissonDiscProvider;
	}
	
	public void setPoissonDiscProvider(IPoissonDiscProvider poissonDiscProvider) {
		this.poissonDiscProvider = poissonDiscProvider;
	}
	
	public IPoissonDiscProvider getPoissonDiscProvider() {
		return poissonDiscProvider;
	}
	
}
