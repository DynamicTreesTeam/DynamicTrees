package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthSubstance implements ISubstanceEffect {
	
	private int duration = 1600;
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
		return true;
	}
	
	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks) {
		
		if (deltaTicks > duration) {
			return false; //Time's up
		}

		if (world.isRemote) {
			if (deltaTicks % 8 == 0) {//Run twinkles every 8 ticks.
				TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 2);
			}
		} else {
			if ((deltaTicks % 40) == 0) {//Grow pulse every 40 ticks
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
	
	public GrowthSubstance setDuration(int duration) {
		this.duration = duration;
		return this;
	}
	
}
