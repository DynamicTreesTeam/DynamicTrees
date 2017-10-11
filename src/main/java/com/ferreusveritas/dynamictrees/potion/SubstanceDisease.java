package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeDisease;
import com.ferreusveritas.dynamictrees.inspectors.NodeTwinkle;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceDisease implements ISubstanceEffect {

	@Override
	public boolean apply(World world, BlockRootyDirt dirt, BlockPos pos) {
		if(world.isRemote) {
			TreeHelper.getSafeTreePart(world, pos.up()).analyse(world, pos.up(), null, new MapSignal(new NodeTwinkle(EnumParticleTypes.CRIT, 8)));
		} else {
			BlockBranch branch = TreeHelper.getBranch(world, pos.up());
			if(branch != null) {
				TreeHelper.getSafeTreePart(world, pos.up()).analyse(world, pos.up(), null, new MapSignal(new NodeDisease(branch.getTree())));
				dirt.fertilize(world, pos, -15);//destroy the soil life so it can no longer grow
			}
		}
		return true;
	}

	@Override
	public boolean update(World world, BlockRootyDirt dirt, BlockPos pos, int deltaTicks) {
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
