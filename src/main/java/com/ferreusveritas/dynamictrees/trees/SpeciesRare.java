package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;

import net.minecraft.util.ResourceLocation;

/**
 * A species that places a TileEntity variation of Saplings and RootyDirtBlocks.
 * Used for more rare species.
 * 
 * This class is deprecated.  Simply use setRequiresTileEntity in Species constructor to achieve the same effect
 * 
 * @author ferreusveritas
 *
 */
@Deprecated
public class SpeciesRare extends Species {
	
	public SpeciesRare(ResourceLocation name, TreeFamily treeFamily) {
		super(name, treeFamily);
		setRequiresTileEntity(true);
	}
	
	public SpeciesRare(ResourceLocation name, TreeFamily treeFamily, ILeavesProperties leavesProperties) {
		super(name, treeFamily, leavesProperties);
		setRequiresTileEntity(true);
	}

}
