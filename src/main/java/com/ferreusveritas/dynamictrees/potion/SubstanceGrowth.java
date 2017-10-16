package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeTwinkle;

import net.minecraft.world.World;

public class SubstanceGrowth implements ISubstanceEffect{

	int duration = 1600;
	
	@Override
	public boolean apply(World world, BlockRootyDirt dirt, BlockPos pos) {
		return true;
	}

	@Override
	public boolean update(World world, BlockRootyDirt dirt, BlockPos pos, int deltaTicks) {

		if(deltaTicks > duration) {
			return false;//Time's up
		}

		if(world.isRemote) {
			if(deltaTicks % 8 == 0) {//Run twinkles every 8 ticks.
				TreeHelper.getSafeTreePart(world, pos.up()).analyse(world, pos.up(), null, new MapSignal(new NodeTwinkle(EnumParticleTypes.SPELL, 2)));
			}
		} else {
			if((deltaTicks % 40) == 0) {//Grow pulse every 40 ticks(2 seconds)
				TreeHelper.growPulse(world, pos);
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

	public SubstanceGrowth setDuration(int duration) {
		this.duration = duration;
		return this;
	}

}
