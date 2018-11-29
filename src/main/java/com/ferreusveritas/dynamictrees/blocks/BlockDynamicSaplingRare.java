package com.ferreusveritas.dynamictrees.blocks;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
		TileEntitySpecies tileEntitySpecies = getTileEntity(access, pos);
		return tileEntitySpecies != null ? tileEntitySpecies.getSpecies() : Species.NULLSPECIES;
	}
	
	
	///////////////////////////////////////////
	// TILE ENTITY STUFF
	///////////////////////////////////////////
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySpecies();
	}
	
	/*
	 * The following is modeled after the harvesting logic flow of flower pots since they too have a
	 * tileEntity that holds items that should be dropped when the block is destroyed.
	 */
	
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		
		if (player.capabilities.isCreativeMode) {
			TileEntitySpecies tileentityspecies = getTileEntity(worldIn, pos);
			if(tileentityspecies != null) {
				tileentityspecies.setSpecies(Species.NULLSPECIES);//Prevents dropping a seed in creative mode
			}
		}
	}
	
	@Nullable
	protected TileEntitySpecies getTileEntity(IBlockAccess access, BlockPos pos) {
		TileEntity tileentity = access.getTileEntity(pos);
		return tileentity instanceof TileEntitySpecies ? (TileEntitySpecies)tileentity : null;
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool) {
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
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
