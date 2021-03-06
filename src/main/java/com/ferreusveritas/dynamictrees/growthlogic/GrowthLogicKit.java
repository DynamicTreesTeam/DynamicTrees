package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class GrowthLogicKit extends ForgeRegistryEntry<GrowthLogicKit> {

	/**
	 * Mods should use this to register their {@link GrowthLogicKit} objects.
	 *
	 * Places the growth logic kits in a central registry.
	 */
	public static IForgeRegistry<GrowthLogicKit> REGISTRY;

	public GrowthLogicKit(final ResourceLocation registryName) {
		this.setRegistryName(registryName);
	}

	public abstract int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap);
	
	public abstract Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal);
	
	public abstract float getEnergy(World world, BlockPos pos, Species species, float signalEnergy);
	
}
