package com.ferreusveritas.dynamictrees.systems.dropcreators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * This works somewhat like a loot table except much more powerful.
 *
 * @author ferreusveritas
 *
 */
public class StorageDropCreator extends DropCreator {

	private final HashMap<ResourceLocation, DropCreator> dropCreators = new HashMap<>();

	public StorageDropCreator() {
		super(DynamicTrees.resLoc("storage"));
	}

	public boolean addDropCreator(DropCreator dropCreator) {
		this.dropCreators.put(dropCreator.getRegistryName(), dropCreator);
		return true;
	}

	public DropCreator findDropCreator(ResourceLocation name) {
		return dropCreators.get(name);
	}

	public boolean remDropCreator(ResourceLocation name) {
		return dropCreators.remove(name) != null;
	}

	public Map<ResourceLocation, DropCreator> getDropCreators() {
		return new HashMap<>(dropCreators);
	}

	private List<ItemStack> makeDropListIfNull(List<ItemStack> dropList) {
		if(dropList == null) {
			dropList = new ArrayList<>();
		}
		return dropList;
	}

	@Override
	protected void registerProperties() { }

	@Override
	public <C extends DropContext> List<ItemStack> appendDrops(ConfiguredDropCreator<DropCreator> configuration, DropType<C> dropType, C context) {
		for (final DropCreator dropCreator : this.dropCreators.values()) {
			dropCreator.appendDrops(null, dropType, context);
		}

		return context.drops();
	}

//	@Override
//	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
//		dropList = makeDropListIfNull(dropList);
//
//		for(DropCreator dropCreator : dropCreators.values()) {
//			dropList = dropCreator.getHarvestDrop(world, species, leafPos, random, dropList, soilLife, fortune);
//		}
//
//		return dropList;
//	}
//
//	@Override
//	public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
//		dropList = makeDropListIfNull(dropList);
//
//		for(DropCreator dropCreator : dropCreators.values()) {
//			dropList = dropCreator.getVoluntaryDrop(world, species, rootPos, random, dropList, soilLife);
//		}
//
//		return dropList;
//	}
//
//	@Override
//	public List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
//		dropList = makeDropListIfNull(dropList);
//
//		for(IDropCreator dropCreator : dropCreators.values()) {
//			dropList = dropCreator.getLeavesDrop(access, species, breakPos, random, dropList, fortune);
//		}
//
//		return dropList;
//	}
//
//	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, NetVolumeNode.Volume volume) {
//		dropList = makeDropListIfNull(dropList);
//
//		for(IDropCreator dropCreator : dropCreators.values()) {
//			dropList = dropCreator.getLogsDrop(world, species, rootPos, random, dropList, volume);
//		}
//
//		return dropList;
//	}

	@Override
	public String toString() {
		return "StorageDropCreator{" +
				"dropCreators=" + dropCreators +
				'}';
	}

}
