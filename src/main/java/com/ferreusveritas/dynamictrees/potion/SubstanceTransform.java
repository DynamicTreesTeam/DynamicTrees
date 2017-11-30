package com.ferreusveritas.dynamictrees.potion;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeTransform;
import com.ferreusveritas.dynamictrees.inspectors.NodeTwinkle;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import net.minecraft.world.World;

public class SubstanceTransform implements ISubstanceEffect {

	DynamicTree toTree;
	
	public SubstanceTransform(DynamicTree toTree) {
		this.toTree = toTree;
	}
	
	@Override
	public boolean apply(World world, BlockRootyDirt dirt, BlockPos pos) {

		if(toTree != null) {
			BlockPos basePos = pos.up();//Position of base of tree
			if(world.isRemote) {
				TreeHelper.getSafeTreePart(world, basePos).analyse(world, basePos, null, new MapSignal(new NodeTwinkle(EnumParticleTypes.CRIT, 8)));
			} else {
				BlockBranch branch = TreeHelper.getBranch(world, basePos);
				if(branch != null) {
					branch.analyse(world, basePos, EnumFacing.DOWN, new MapSignal(new NodeTransform(branch.getTree(), toTree), new NodeTwinkle(EnumParticleTypes.FIREWORKS_SPARK, 8)));
				}
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
		return "transform";
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
