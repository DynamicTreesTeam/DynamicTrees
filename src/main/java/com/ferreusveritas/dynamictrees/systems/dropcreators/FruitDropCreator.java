package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * A drop creator that drops fruit just like Vanilla apples.
 *
 * @author ferreusveritas
 */
public class FruitDropCreator extends DropCreator {

	public static final ConfigurationProperty<ItemStack> FRUIT = ConfigurationProperty.property("fruit", ItemStack.class);

	public FruitDropCreator(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(FRUIT, RARITY);
	}

	@Override
	protected ConfiguredDropCreator<DropCreator> createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(FRUIT, new ItemStack(Items.APPLE))
				.with(RARITY, 1f);
	}

	@Override
	public void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		this.appendFruit(context.drops(), configuration, context);
	}

	@Override
	public void appendLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		this.appendFruit(context.drops(), configuration, context);
	}

	private void appendFruit(List<ItemStack> dropList, ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		// More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; // 1 in 200 chance of returning an "apple"
		if (context.fortune() > 0) {
			chance -= 10 << context.fortune();
			if (chance < 40) {
				chance = 40;
			}
		}

		if (context.random().nextInt((int) (chance / configuration.get(RARITY))) == 0) {
			dropList.add(configuration.get(FRUIT));
		}
	}

}
