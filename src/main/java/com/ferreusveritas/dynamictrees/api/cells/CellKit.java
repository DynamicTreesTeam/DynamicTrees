package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import com.ferreusveritas.dynamictrees.init.DTTrees;

public abstract class CellKit extends ForgeRegistryEntry<CellKit> {

	/**
	 * The registry. This is used for registering and querying {@link CellKit} objects.
	 *
	 * <p>Add-ons should use {@link DTTrees.CellKitRegistryEvent}, <b>not</b> Forge's registry event.</p>
	 */
	public static IForgeRegistry<CellKit> REGISTRY;

	public CellKit(final ResourceLocation registryName) {
		this.setRegistryName(registryName);
	}

	public abstract ICell getCellForLeaves(int distance);

	public abstract ICell getCellForBranch(int radius, int meta);

	public abstract ICellSolver getCellSolver();
	
	/** A voxel map of leaves blocks that are "stamped" on to the tree during generation */
	public abstract SimpleVoxmap getLeafCluster();
	
	/** The default hydration level of a newly created leaf block [default = 4]**/
	public abstract int getDefaultHydration();

	@Override
	public String toString() {
		return "CellKit{registryName=" + this.getRegistryName() + "}";
	}
}
