package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemappers.TransformNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransformSubstance implements ISubstanceEffect {
	
	final Species toSpecies;
	
	public TransformSubstance(final Species toTree) {
		this.toSpecies = toTree;
	}
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {

		final BlockState rootyState = world.getBlockState(rootPos);
		final RootyBlock dirt = TreeHelper.getRooty(rootyState);

		if (dirt != null && toSpecies != null) {
			Species fromSpecies = dirt.getSpecies(rootyState, world, rootPos);
			if (fromSpecies.isTransformable() && fromSpecies!= toSpecies) {
				if (world.isClientSide) {
					TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
				} else {
					dirt.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));
				}
				return true;
			}
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
