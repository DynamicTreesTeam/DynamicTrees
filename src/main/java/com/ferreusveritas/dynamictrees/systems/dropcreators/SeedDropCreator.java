package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.event.VoluntarySeedDropEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class SeedDropCreator extends DropCreator {

	public static final ConfigurationProperty<Float> RARITY = ConfigurationProperty.floatProperty("rarity");
	public static final ConfigurationProperty<Float> HARVEST_RARITY = ConfigurationProperty.floatProperty("harvest_rarity");
	public static final ConfigurationProperty<Float> VOLUNTARY_RARITY = ConfigurationProperty.floatProperty("voluntary_rarity");
	public static final ConfigurationProperty<Float> LEAVES_RARITY = ConfigurationProperty.floatProperty("leaves_rarity");
	/** Allows a custom seed to be set, for example, tree A may want to drop the seed of tree B. */
	public static final ConfigurationProperty<ItemStack> SEED = ConfigurationProperty.property("seed", ItemStack.class);

	public SeedDropCreator(ResourceLocation registryName) {
		super(registryName);
	}

	//Allows for overriding species seed drop if a custom seed is set.
	protected ItemStack getSeedStack(Species species, ConfiguredDropCreator<DropCreator> configuration) {
		final ItemStack customSeed = configuration.get(SEED);
		if (customSeed.isEmpty()) {
			return species.getSeedStack(1);
		} else {
			return customSeed;
		}
	}

	@Override
	protected void registerProperties() {
		this.register(RARITY, HARVEST_RARITY, VOLUNTARY_RARITY, LEAVES_RARITY, SEED);
	}

	@Override
	protected ConfiguredDropCreator<DropCreator> createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(RARITY, 1f)
				.with(HARVEST_RARITY, -1f)
				.with(VOLUNTARY_RARITY, -1f)
				.with(LEAVES_RARITY, -1f)
				.with(SEED, ItemStack.EMPTY);
	}

	private float rarityOrDefault(ConfiguredDropCreator<DropCreator> configuration, ConfigurationProperty<Float> rarityProperty) {
		final float rarityOverride = configuration.get(rarityProperty);
		return rarityOverride == -1f ? configuration.get(RARITY) : rarityOverride;
	}

	@Override
	public void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		float rarity = this.rarityOrDefault(configuration, HARVEST_RARITY);
		rarity *= (context.fortune() + 1) / 64f;
		rarity *= Math.min(context.species().seasonalSeedDropFactor(context.world(), context.pos()) + 0.15f, 1.0);

		if (rarity > context.random().nextFloat()) {//1 in 64 chance to drop a seed on destruction..
			context.drops().add(getSeedStack(context.species(), configuration));
		}
	}

	@Override
	public void appendVoluntaryDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		if (this.rarityOrDefault(configuration, VOLUNTARY_RARITY) * DTConfigs.SEED_DROP_RATE.get() *
				context.species().seasonalSeedDropFactor(context.world(), context.pos())
				> context.random().nextFloat()) {
			context.drops().add(getSeedStack(context.species(), configuration));
			VoluntarySeedDropEvent seedDropEvent = new VoluntarySeedDropEvent(context.world(), context.pos(), context.species(), context.drops());
			MinecraftForge.EVENT_BUS.post(seedDropEvent);
			if (seedDropEvent.isCanceled()) {
				context.drops().clear();
			}
		}
	}

	@Override
	public void appendLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
		int chance = 20; //See BlockLeaves#getSaplingDropChance(state);
		//Hokey fortune stuff here to match Vanilla logic.
		if (context.fortune() > 0) {
			chance -= 2 << context.fortune();
			if (chance < 10) {
				chance = 10;
			}
		}

		float seasonFactor = 1.0f;

		if(!context.world().isClientSide) {
			seasonFactor = context.species().seasonalSeedDropFactor(context.world(), context.pos());
		}

		if (context.random().nextInt((int) (chance / this.rarityOrDefault(configuration, LEAVES_RARITY))) == 0) {
			if (seasonFactor > context.random().nextFloat()) {
				context.drops().add(this.getSeedStack(context.species(), configuration));
			}
		}
	}

}
