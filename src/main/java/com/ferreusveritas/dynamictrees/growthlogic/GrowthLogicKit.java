package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Registry;
import com.ferreusveritas.dynamictrees.util.RegistryEntry;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GrowthLogicKit extends RegistryEntry<GrowthLogicKit> {

	public static final GrowthLogicKit NULL_LOGIC = new GrowthLogicKit(DTTrees.NULL) {
		@Override public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) { return probMap; }
		@Override public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) { return newDir; }
		@Override public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) { return signalEnergy; }
	};

	/**
	 * Central registry for all {@link GrowthLogicKit} objects.
	 */
	public static final Registry<GrowthLogicKit> REGISTRY = new Registry<>(GrowthLogicKit.class, NULL_LOGIC);

	public GrowthLogicKit(final ResourceLocation registryName) {
		this.setRegistryName(registryName);
	}

	public abstract int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap);
	
	public abstract Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal);
	
	public abstract float getEnergy(World world, BlockPos pos, Species species, float signalEnergy);
	
}
