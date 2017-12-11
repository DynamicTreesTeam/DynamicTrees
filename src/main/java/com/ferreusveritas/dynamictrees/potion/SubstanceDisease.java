package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeDisease;
import com.ferreusveritas.dynamictrees.inspectors.NodeTwinkle;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceDisease implements ISubstanceEffect {

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);
		BlockPos basePos = rootPos.up();//Position of base of tree
		if(world.isRemote) {
			TreeHelper.getSafeTreePart(world, basePos).analyse(world, basePos, null, new MapSignal(new NodeTwinkle(EnumParticleTypes.CRIT, 8)));
		} else {
			BlockBranch branch = TreeHelper.getBranch(world, basePos);
			if(branch != null) {
				Species species = DynamicTree.getExactSpecies(world, basePos);
				TreeHelper.getSafeTreePart(world, basePos).analyse(world, basePos, null, new MapSignal(new NodeDisease(species)));
				dirt.fertilize(world, rootPos, -15);//destroy the soil life so it can no longer grow
			}
		}
		return true;
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
