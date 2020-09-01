package com.ferreusveritas.dynamictrees.trees;

//import com.ferreusveritas.dynamictrees.DTRegistries.
//import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
//import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
//import com.ferreusveritas.dynamictrees.init.ModRegistries;
//import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;

import com.ferreusveritas.dynamictrees.api.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * A species that places a TileEntity variation of Saplings and RootyDirtBlocks.
 * Used for more rare species.
 * 
 * @author ferreusveritas
 *
 */
public class SpeciesRare extends Species {

	public SpeciesRare(ResourceLocation name, TreeFamily treeFamily) {
		super(name, treeFamily);
	}

	public SpeciesRare(ResourceLocation name, TreeFamily treeFamily, ILeavesProperties leavesProperties) {
		super(name, treeFamily, leavesProperties);
	}

	public boolean transitionToTree(World world, BlockPos pos) {
		if (super.transitionToTree(world,pos)){
			world.setTileEntity(pos.down(), DTRegistries.speciesTE.create());
			TileEntity tileEntity = world.getTileEntity(pos.down()); // we set the TE and then we look for it in case it failed
			if(tileEntity instanceof TileEntitySpecies) {
				TileEntitySpecies speciesTE = (TileEntitySpecies) tileEntity;
				speciesTE.setSpecies(this);
			}
			return true;
		}
		return false;
	}

}
