package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthSubstance implements ISubstanceEffect {

	private final int pulses;
	private final int ticksPerPulse;
	private final int ticksPerParticlePulse = 8;

	public GrowthSubstance() {
		this(-1, 24);
	}

	public GrowthSubstance(int pulses, int ticksPerPulse) {
		this.pulses = pulses;
		this.ticksPerPulse = ticksPerPulse;
	}

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		new FertilizeSubstance().setAmount(15).setDisplayParticles(false).apply(world, rootPos);
		TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 8);
		return true;
	}

	private int pulseCount;

	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
		// Stop when fertility has depleted.
		if (fertility <= 0 || this.pulseCount >= this.pulses)
			return false;

		if (world.isClientSide) {
			if (deltaTicks % this.ticksPerParticlePulse == 0) {
				TreeHelper.rootParticles(world, rootPos, Direction.UP, ParticleTypes.EFFECT, 1);
			}
		} else {
			if (deltaTicks % this.ticksPerPulse == 0) {
				TreeHelper.growPulse(world, rootPos);
				this.pulseCount++;
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
