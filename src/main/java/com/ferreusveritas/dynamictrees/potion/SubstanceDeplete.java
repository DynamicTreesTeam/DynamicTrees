package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeTwinkle;

import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public class SubstanceDeplete implements ISubstanceEffect {

	int amount;
	
	@Override
	public boolean apply(World world, BlockRootyDirt dirt, BlockPos pos) {
		if(dirt.fertilize(world, pos, -amount)) {
			if(world.isRemote()) {
				TreeHelper.getSafeTreePart(world, pos.up()).analyse(world, pos.up(), null, new MapSignal(new NodeTwinkle(EnumParticleTypes.VILLAGER_ANGRY, 8)));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean update(World world, BlockRootyDirt dirt, BlockPos pos, int deltaTicks) {
		return false;
	}
	
	@Override
	public String getName() {
		return "deplete";
	}
	
	public SubstanceDeplete setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public boolean isLingering() {
		return false;
	}
	
}
