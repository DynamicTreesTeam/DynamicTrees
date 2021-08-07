package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class SubstanceFertilize implements ISubstanceEffect {

	private int amount = 2;
	private boolean grow;
	private int pulses = 1;

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		BlockRooty dirt = TreeHelper.getRooty(world.getBlockState(rootPos));
		if (dirt != null && dirt.fertilize(world, rootPos, amount) || grow) {
			if (world.isRemote) {
				TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.VILLAGER_HAPPY, 8);
			} else {
				if (grow) {
					for (int i = 0; i < pulses; i++) {
						TreeHelper.growPulse(world, rootPos);
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
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
	 * If growth is enabled then the tree will take an update and the item will be consumed.  Regardless of if the soil
	 * life is full.
	 *
	 * @param grow
	 * @return
	 */
	public SubstanceFertilize setGrow(boolean grow) {
		this.grow = grow;
		return this;
	}

	public SubstanceFertilize setPulses(int pulses) {
		this.pulses = pulses;
		return this;
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
