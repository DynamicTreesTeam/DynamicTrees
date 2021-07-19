package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This works somewhat like a loot table except much more powerful.
 *
 * @author ferreusveritas
 */
public class StorageDropCreator extends DropCreator {

	private final List<ConfiguredDropCreator<DropCreator>> dropCreators = new LinkedList<>();

	public StorageDropCreator() {
		super(DynamicTrees.resLoc("storage"));
	}

	public boolean addDropCreator(DropCreator dropCreator) {
		return this.dropCreators.add(dropCreator.getDefaultConfiguration());
	}

	@SuppressWarnings("unchecked")
	public <DC extends DropCreator> boolean addDropCreator(ConfiguredDropCreator<DC> dropCreator) {
		return this.dropCreators.add((ConfiguredDropCreator<DropCreator>) dropCreator);
	}

	public Optional<ConfiguredDropCreator<DropCreator>> findDropCreator(ResourceLocation registryName) {
		return dropCreators.stream()
				.filter(configuration -> configuration.getConfigurable().getRegistryName().equals(registryName))
				.findFirst();
	}

	public List<ConfiguredDropCreator<DropCreator>> findDropCreators(ResourceLocation registryName) {
		return dropCreators.stream()
				.filter(configuration -> configuration.getConfigurable().getRegistryName().equals(registryName))
				.collect(Collectors.toList());
	}

	public boolean removeDropCreator(ResourceLocation registryName) {
		return this.dropCreators.removeIf(configuration ->
				configuration.getConfigurable().getRegistryName().equals(registryName));
	}

	public List<ConfiguredDropCreator<DropCreator>> getDropCreators() {
		return new LinkedList<>(this.dropCreators);
	}

	@Override
	protected void registerProperties() { }

	@Override
	public <C extends DropContext> void appendDrops(ConfiguredDropCreator<DropCreator> nothing, DropType<C> dropType, C context) {
		this.dropCreators.forEach(configuration ->
				configuration.getConfigurable().appendDrops(configuration, dropType, context));
	}

	@Override
	public String toString() {
		return "StorageDropCreator{" +
				"dropCreators=" + dropCreators +
				'}';
	}

}
