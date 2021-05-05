package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.Configurable;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
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
public class DropCreator extends RegistryEntry<DropCreator> implements IResettable<DropCreator>, Configurable {

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

	public static final DropCreator NULL_DROP_CREATOR = new DropCreator(DTTrees.NULL);

	public static final TypedRegistry.EntryType<DropCreator> TYPE = TypedRegistry.newType(DropCreator::new);

	public static final TypedRegistry<DropCreator> REGISTRY = new TypedRegistry<>(DropCreator.class, NULL_DROP_CREATOR, TYPE);

	public DropCreator(final ResourceLocation registryName) {
		super(registryName);
	}

	public <C extends DropContext> List<ItemStack> appendDrops(final DropType<C> dropType, final C context) {
		if (dropType == DropType.HARVEST)
			this.getHarvestDrop(context.world(), context.species(), context.pos(), context.random(), context.drops(), context.soilLife(), context.fortune());
		else if (dropType == DropType.VOLUNTARY)
			this.getVoluntaryDrop(context.world(), context.species(), context.pos(), context.random(), context.drops(), context.soilLife());
		else if (dropType == DropType.LEAVES)
			this.getLeavesDrop(context.world(), context.species(), context.pos(), context.random(), context.drops(), context.fortune());
		else if (dropType == DropType.LOGS)
			this.getLogsDrop(context.world(), context.species(), context.pos(), context.random(), context.drops(), ((LogDropContext) context).volume());

		return context.drops();
	}
	
	protected List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		return dropList;
	}

	protected List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
		return dropList;
	}

	protected List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		return dropList;
	}

	protected List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NetVolumeNode.Volume volume) {
		return dropList;
	}

	@Override
	public boolean isPropertyRegistered(ConfigurationProperty<?> property) {
		return false;
	}

}
