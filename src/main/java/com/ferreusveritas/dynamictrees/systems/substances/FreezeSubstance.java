package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFreezer;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FreezeSubstance implements ISubstanceEffect {
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
 		final BlockState rootyState = world.getBlockState(rootPos);
 		final RootyBlock dirt = TreeHelper.getRooty(rootyState);
 		final Species species = dirt.getSpecies(rootyState, world, rootPos);

 		if (species != Species.NULL_SPECIES && dirt != null) {
 			if (world.isRemote) {
 				TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
 			} else {
 				dirt.startAnalysis(world, rootPos, new MapSignal(new NodeFreezer(species)));
 				dirt.fertilize(world, rootPos, -15);//destroy the soil life so it can no longer grow
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
		return "freeze";
	}
	
	@Override
	public boolean isLingering() {
		return false;
	}
	
}
