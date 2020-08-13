package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceFertilize implements ISubstanceEffect {
	
	int amount = 1;
	boolean grow;
	
	@Override
	public boolean apply(World world, BlockPos rootPos) {
//		BlockRooty dirt = TreeHelper.getRooty(world.getBlockState(rootPos));
//		if(dirt != null && dirt.fertilize(world, rootPos, amount) || grow) {
//			if(world.isRemote) {
//				TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.VILLAGER_HAPPY, 8);
//			} else {
//				if(grow) {
//					TreeHelper.growPulse(world, rootPos);
//				}
//			}
//			return true;
//		}
		return false;
	}
	
	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks) {
		return false;
	}
	
	@Override
	public String getName() {
		return "fertilize";
	}
	
	public SubstanceFertilize setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * If growth is enabled then the tree will take an
	 * update and the item will be consumed.  Regardless
	 * of if the soil life is full.
	 * 
	 * @param grow
	 * @return
	 */
	public SubstanceFertilize setGrow(boolean grow) {
		this.grow = grow;
		return this;
	}
	
	@Override
	public boolean isLingering() {
		return false;
	}
	
}
