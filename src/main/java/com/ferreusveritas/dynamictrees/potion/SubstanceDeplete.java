package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;

public class SubstanceDeplete implements ISubstanceEffect {

	int amount;
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);
		if(dirt.fertilize(world, rootPos, -amount)) {
			TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.VILLAGER_ANGRY, 8);
			return true;
		}
		return false;
	}

	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks) {
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
