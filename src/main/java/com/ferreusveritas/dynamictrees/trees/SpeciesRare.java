package com.ferreusveritas.dynamictrees.trees;

//import com.ferreusveritas.dynamictrees.ModBlocks;
//import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
//import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
//import com.ferreusveritas.dynamictrees.init.ModRegistries;
//import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import net.minecraft.util.ResourceLocation;

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

//	@Override
//	public boolean plantSapling(World world, BlockPos pos) {
//		super.plantSapling(world, pos);
//		TileEntity tileEntity = world.getTileEntity(pos);
//		if(tileEntity instanceof TileEntitySpecies) {
//			TileEntitySpecies speciesTE = (TileEntitySpecies) tileEntity;
//			speciesTE.setSpecies(this);
//			return true;
//		}
//		return false;
//	}

//	@Override
//	public BlockRooty getRootyBlock() {
//		return ModRegistries.blockRootyDirtSpecies;
//	}

//	@Override
//	public boolean placeRootyDirtBlock(World world, BlockPos rootPos, int life) {
//		super.placeRootyDirtBlock(world, rootPos, life);
//		TileEntity tileEntity = world.getTileEntity(rootPos);
//		if(tileEntity instanceof TileEntitySpecies) {
//			TileEntitySpecies speciesTE = (TileEntitySpecies) tileEntity;
//			speciesTE.setSpecies(this);
//			return true;
//		}
//		return true;
//	}

}
