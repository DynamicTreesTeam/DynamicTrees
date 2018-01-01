package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDisease;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceDisease implements ISubstanceEffect {

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);
		if(dirt != null) {
			Species species = dirt.getSpecies(world, rootPos);
			if(species != null) {
				if(world.isRemote) {
					TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.CRIT, 8);
				} else {
					dirt.startAnalysis(world, rootPos, new MapSignal(new NodeDisease(species)));
					dirt.fertilize(world, rootPos, -15);//destroy the soil life so it can no longer grow
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
		return "disease";
	}
	
	@Override
	public boolean isLingering() {
		return false;
	}

}
