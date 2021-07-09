package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Random;

/**
 * A drop creator for the sticks that can be harvested from leaves.
 * Rarity of 1 equals a 1/50 chance of getting between {1 - maxCount} sticks.
 *
 * @author Max Hyper
 */
public class SticksDropCreator extends DropCreator {
	ItemStack droppedItem;
	float rarity;
	int maxCount;

	public SticksDropCreator(Species species) {
		this(species, 1, 2);
	}

	public SticksDropCreator(Species species, float rarity, int maxCount) {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "sticks"));
		this.droppedItem = species.getFamily().getStick(1);
		this.rarity = rarity;
		this.maxCount = Math.max(1, maxCount);
	}

	@Override
	protected void registerProperties() {

	}

	@Override
	public void appendLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		this.appendSticks(context.drops(), context.random(), context.fortune());
	}

	@Override
	public void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		this.appendSticks(context.drops(), context.random(), 0);
	}

	private void appendSticks(List<ItemStack> dropList, Random random, int fortune){
		int chance = 50;
		if (fortune > 0) {
			chance -= 2 << fortune;
			if (chance < 10)
				chance = 10;
		}
		if (random.nextInt((int) (chance / rarity)) == 0) {
			ItemStack drop = droppedItem.copy();
			drop.setCount(1 + random.nextInt(maxCount));
			dropList.add(drop);
		}
	}

}
