package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * This exists solely to aid in the creation of a cleaner anonymous class.
 * All of the members in this class act as pass-thrus by default.
 *
 * @author ferreusveritas
 */
public abstract class DropCreator extends ConfigurableRegistryEntry<DropCreator, ConfiguredDropCreator<DropCreator>> implements IResettable<DropCreator> {

	public static final class DropType<C extends DropContext> extends RegistryEntry<DropType<C>> {
		public static final DropType<DropContext> NULL = new DropType<>(DTTrees.NULL);

		public static final DropType<DropContext> HARVEST = new DropType<>(DynamicTrees.resLoc("harvest"));
		public static final DropType<DropContext> VOLUNTARY = new DropType<>(DynamicTrees.resLoc("voluntary"));
		public static final DropType<DropContext> LEAVES = new DropType<>(DynamicTrees.resLoc("leaves"));
		public static final DropType<LogDropContext> LOGS = new DropType<>(DynamicTrees.resLoc("logs"));

		@SuppressWarnings("unchecked")
		public static final Class<DropType<DropContext>> TYPE = (Class<DropType<DropContext>>) NULL.getClass();

		public static final Registry<DropType<DropContext>> REGISTRY = new Registry<>(TYPE, NULL);

		public DropType(ResourceLocation registryName) {
			super(registryName);
		}
	}

	public static final DropCreator NULL_DROP_CREATOR = new DropCreator(DTTrees.NULL) {
		@Override protected void registerProperties() { }
	};

	public static final Registry<DropCreator> REGISTRY = new Registry<>(DropCreator.class, NULL_DROP_CREATOR);

	public DropCreator(final ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	protected ConfiguredDropCreator<DropCreator> createDefaultConfiguration() {
		return new ConfiguredDropCreator<>(this);
	}

	public <C extends DropContext> List<ItemStack> appendDrops(final ConfiguredDropCreator<DropCreator> configuration, final DropType<C> dropType, final C context) {
		if (dropType == DropType.HARVEST)
			this.getHarvestDrops(configuration, context.world(), context.species(), context.pos(), context.random(), context.drops(), context.fertility(), context.fortune());
		else if (dropType == DropType.VOLUNTARY)
			this.getVoluntaryDrops(configuration, context.world(), context.species(), context.pos(), context.random(), context.drops(), context.fertility());
		else if (dropType == DropType.LEAVES)
			this.getLeavesDrops(configuration, context.world(), context.species(), context.pos(), context.random(), context.drops(), context.fortune());
		else if (dropType == DropType.LOGS)
			this.getLogsDrops(configuration, context.world(), context.species(), context.pos(), context.random(), context.drops(), ((LogDropContext) context).volume());

		return context.drops();
	}

	protected List<ItemStack> getHarvestDrops(final ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos leafPos, Random random, List<ItemStack> drops, int fertility, int fortune) {
		return drops;
	}

	protected List<ItemStack> getVoluntaryDrops(final ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos rootPos, Random random, List<ItemStack> drops, int fertility) {
		return drops;
	}

	protected List<ItemStack> getLeavesDrops(final ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, int fortune) {
		return drops;
	}

	protected List<ItemStack> getLogsDrops(final ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, NetVolumeNode.Volume volume) {
		return drops;
	}

	@Override
	public boolean isPropertyRegistered(ConfigurationProperty<?> property) {
		return false;
	}

}
