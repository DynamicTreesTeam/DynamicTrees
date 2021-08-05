package com.ferreusveritas.dynamictrees.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExtendedBlockStateWater extends BlockStateContainer {
	private final ImmutableSet<IUnlistedProperty<?>> unlistedProperties;

	public ExtendedBlockStateWater(Block blockIn, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
		super(blockIn, properties, buildUnlistedMap(unlistedProperties));
		ImmutableSet.Builder<IUnlistedProperty<?>> builder = ImmutableSet.builder();
		for (IUnlistedProperty<?> property : unlistedProperties) {
			builder.add(property);
		}
		this.unlistedProperties = builder.build();
	}

	public Collection<IUnlistedProperty<?>> getUnlistedProperties() {
		return unlistedProperties;
	}

	private static ImmutableMap<IUnlistedProperty<?>, Optional<?>> buildUnlistedMap(IUnlistedProperty<?>[] unlistedProperties) {
		ImmutableMap.Builder<IUnlistedProperty<?>, Optional<?>> builder = ImmutableMap.builder();
		for (IUnlistedProperty<?> p : unlistedProperties) {
			builder.put(p, Optional.empty());
		}
		return builder.build();
	}

	@Override
	@Nonnull
	protected StateImplementation createState(@Nonnull Block block, @Nonnull ImmutableMap<IProperty<?>, Comparable<?>> properties, @Nullable ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		if (unlistedProperties == null || unlistedProperties.isEmpty()) {
			return super.createState(block, properties, unlistedProperties);
		}
		return new ExtendedStateImplementationWater(block, properties, unlistedProperties, null, null);
	}

	protected static class ExtendedStateImplementationWater extends StateImplementation implements IExtendedBlockState {
		private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
		private final IBlockState cleanState;

		protected ExtendedStateImplementationWater(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, @Nullable ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> table, IBlockState clean) {
			super(block, properties, table);
			this.unlistedProperties = unlistedProperties;
			this.cleanState = clean == null ? this : clean;
		}

		/**
		 * Get a version of this BlockState with the given Property now set to the given value
		 */
		@Override
		@Nonnull
		public <T extends Comparable<T>, V extends T> IBlockState withProperty(@Nonnull IProperty<T> property, @Nonnull V value) {
			IBlockState clean = super.withProperty(property, value);
			if (clean == this.cleanState) {
				return this;
			}

			if (this == this.cleanState) { // no dynamic properties present, looking up in the normal table
				return clean;
			}

			return new ExtendedStateImplementationWater(getBlock(), clean.getProperties(), unlistedProperties, ((StateImplementation) clean).getPropertyValueTable(), this.cleanState);
		}

		@Override
		public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, @Nullable V value) {
			Optional<?> oldValue = unlistedProperties.get(property);
			if (oldValue == null) {
				throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
			}
			if (Objects.equals(oldValue.orElse(null), value)) {
				return this;
			}
			if (!property.isValid(value)) {
				throw new IllegalArgumentException("Cannot set unlisted property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
			}
			boolean clean = true;
			ImmutableMap.Builder<IUnlistedProperty<?>, Optional<?>> builder = ImmutableMap.builder();
			for (Map.Entry<IUnlistedProperty<?>, Optional<?>> entry : unlistedProperties.entrySet()) {
				IUnlistedProperty<?> key = entry.getKey();
				Optional<?> newValue = key.equals(property) ? Optional.ofNullable(value) : entry.getValue();
				if (newValue.isPresent()) {
					clean = false;
				}
				builder.put(key, newValue);
			}
			if (clean) { // no dynamic properties, lookup normal state
				return (IExtendedBlockState) cleanState;
			}
			return new ExtendedStateImplementationWater(getBlock(), getProperties(), builder.build(), propertyValueTable, this.cleanState);
		}

		@Override
		public Collection<IUnlistedProperty<?>> getUnlistedNames() {
			return unlistedProperties.keySet();
		}

		@Override
		public <T extends Comparable<T>> T getValue(IProperty<T> property) {
			if (property == BlockLiquid.LEVEL) {
				return property.getValueClass().cast(Integer.valueOf(15));
			}
			return super.getValue(property);
		}

		@Override
		@Nullable
		public <V> V getValue(IUnlistedProperty<V> property) {
			Optional<?> value = unlistedProperties.get(property);
			if (value == null) {
				throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
			}
			return property.getType().cast(value.orElse(null));
		}

		public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties() {
			return unlistedProperties;
		}

		@Override
		public IBlockState getClean() {
			return cleanState;
		}
	}
}
