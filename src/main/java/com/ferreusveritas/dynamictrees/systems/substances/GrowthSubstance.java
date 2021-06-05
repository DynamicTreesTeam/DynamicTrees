package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthSubstance implements ISubstanceEffect {

	private final int ticksPerPulse = 24;
	private final int ticksPerParticlePulse = 8;

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		new FertilizeSubstance().setAmount(15).setDisplayParticles(false).apply(world, rootPos);
		TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 8);
		return true;
	}
	
	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
		// Stop when fertility has depleted.
		if (fertility <= 0)
			return false;

		if (world.isClientSide) {
			if (deltaTicks % this.ticksPerParticlePulse == 0) {
				TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 2);
			}
		} else {
			if (deltaTicks % this.ticksPerPulse == 0) {
				TreeHelper.growPulse(world, rootPos);
			}
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
