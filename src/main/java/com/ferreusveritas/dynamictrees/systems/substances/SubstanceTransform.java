package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeTransform;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceTransform implements ISubstanceEffect {
	
	final Species toSpecies;
	
	public SubstanceTransform (final Species toTree) {
		this.toSpecies = toTree;
	}
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {

		final BlockState rootyState = world.getBlockState(rootPos);
		final BlockRooty dirt = TreeHelper.getRooty(rootyState);

		if (dirt != null && toSpecies != null) {
			Species fromSpecies = dirt.getSpecies(rootyState, world, rootPos);
			if (fromSpecies != Species.NULLSPECIES && fromSpecies.getRegistryName() != toSpecies.getRegistryName()) {
				if (world.isRemote) {
					TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
				} else {
					dirt.startAnalysis(world, rootPos, new MapSignal(new NodeTransform(fromSpecies, toSpecies)));
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
