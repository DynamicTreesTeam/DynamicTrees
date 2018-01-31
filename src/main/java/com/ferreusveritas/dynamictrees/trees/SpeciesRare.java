package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A species that places a TileEntity variation of Saplings and RootyDirtBlocks.
 * Used for more rare species.
 * 
 * @author ferreusveritas
 *
 */
public class SpeciesRare extends Species {

	public SpeciesRare(ResourceLocation name, DynamicTree treeFamily) {
		super(name, treeFamily);
	}
	
	@Override
	public boolean plantSapling(World world, BlockPos pos) {
		super.plantSapling(world, pos);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntitySpecies) {
			TileEntitySpecies speciesTE = (TileEntitySpecies) tileEntity;
			speciesTE.setSpecies(this);
			return true;
		}
		return false;
	}
	
	@Override
	public BlockRootyDirt getRootyDirtBlock() {
		return ModBlocks.blockRootyDirtSpecies;
	}
	
	@Override
	public boolean placeRootyDirtBlock(World world, BlockPos rootPos, int life) {
		super.placeRootyDirtBlock(world, rootPos, life);
		TileEntity tileEntity = world.getTileEntity(rootPos);
		if(tileEntity instanceof TileEntitySpecies) {
			TileEntitySpecies speciesTE = (TileEntitySpecies) tileEntity;
			speciesTE.setSpecies(this);
			return true;
		}
		return true;
	}
	
}
