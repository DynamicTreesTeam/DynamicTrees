package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDynamicSaplingRare extends BlockDynamicSapling implements ITileEntityProvider {
	
	public BlockDynamicSaplingRare(String name) {
		super(name);
		hasTileEntity = true;
	}
	
	@Override
	public BlockDynamicSapling setSpecies(IBlockState state, Species species) {
		//A tile entity version of the dynamic sapling does not contain a blockState mapped reference to a species.
		//The species is determined at runtime by tileEntity data.
		return this;
	}
	
	@Override
	public Species getSpecies(IBlockAccess access, BlockPos pos, IBlockState state) {
		TileEntity tileEntity = access.getTileEntity(pos);
		if(tileEntity instanceof TileEntitySpecies) {
			TileEntitySpecies tileEntitySpecies = (TileEntitySpecies) tileEntity;
			return tileEntitySpecies.getSpecies();
		}
		return Species.NULLSPECIES;
	}
	
	
	///////////////////////////////////////////
	// TILE ENTITY STUFF
	///////////////////////////////////////////
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySpecies();
	}
	
    /** Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated */
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
    }
	
    /**
     * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
     * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
     * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
     */
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
    }
    
}
