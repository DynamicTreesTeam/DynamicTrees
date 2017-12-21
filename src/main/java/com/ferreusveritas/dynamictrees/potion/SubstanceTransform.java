package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeTransform;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

public class SubstanceTransform implements ISubstanceEffect {

	DynamicTree toTree;
	
	public SubstanceTransform(DynamicTree toTree) {
		this.toTree = toTree;
	}
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {

		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);

		if(dirt != null && toTree != null) {
			if(world.isRemote) {
				TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.FIREWORKS_SPARK, 8);
			} else {
				Species species = dirt.getSpecies(world, rootPos);
				if(species != null) {
					dirt.startAnalysis(world, rootPos, new MapSignal(new NodeTransform(species.getTree(), toTree)));
				}
			}
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
		return "transform";
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
