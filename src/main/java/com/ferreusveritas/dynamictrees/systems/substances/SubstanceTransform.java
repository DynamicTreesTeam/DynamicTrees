package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.compat.WailaOther;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeTransform;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceTransform implements ISubstanceEffect {
	
	Species toSpecies;
	
	public SubstanceTransform(Species toTree) {
		this.toSpecies = toTree;
	}
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
		
		IBlockState rootyState = world.getBlockState(rootPos);
		BlockRooty dirt = TreeHelper.getRooty(rootyState);

		if (dirt != null && toSpecies != null) {
			Species fromSpecies = dirt.getSpecies(rootyState, world, rootPos);
			if (fromSpecies.isTransformable() && fromSpecies != toSpecies) {
				if (world.isRemote) {
					TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.FIREWORKS_SPARK, 8);
					WailaOther.invalidateWailaPosition();
				} else {
					dirt.startAnalysis(world, rootPos, new MapSignal(new NodeTransform(fromSpecies, toSpecies)));
					
					if (dirt.getSpecies(rootyState, world, rootPos) != toSpecies) {
						toSpecies.placeRootyDirtBlock(world, rootPos, dirt.getSoilLife(rootyState, world, rootPos));
					}
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
