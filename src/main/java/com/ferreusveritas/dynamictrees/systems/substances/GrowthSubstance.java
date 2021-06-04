package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthSubstance implements ISubstanceEffect {
	
	//private int duration = 1600;
	private int ticksPerPulse = 30;
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
		new FertilizeSubstance().setAmount(15).setDisplayParticles(false).apply(world, rootPos);
		TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 8);
		return true;
	}
	
	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
		
		if (fertility <= 0)
			return false; // No more fertility.

		if ((deltaTicks % ticksPerPulse) == 0) {
			if (world.isClientSide)
				TreeHelper.rootParticles(world, rootPos, Direction.UP, ParticleTypes.EFFECT, 1);
			else
				TreeHelper.growPulse(world, rootPos);
		}
		
		return true;
	}
	
	@Override
	public String getName() {
		return "growth";
	}
	
	@Override
	public boolean isLingering() {
		return true;
	}

}
