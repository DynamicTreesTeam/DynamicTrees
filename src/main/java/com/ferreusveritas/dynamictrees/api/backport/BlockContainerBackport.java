package com.ferreusveritas.dynamictrees.api.backport;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public abstract class BlockContainerBackport extends BlockBackport implements ITileEntityProvider {

	protected BlockContainerBackport(Material material) {
		super(material);
		this.isBlockContainer = true;
	}
	
	//////////////////////////////
	// BLOCK CONTAINER LOGIC
	//////////////////////////////

	/** Called whenever the block is added into the world. */
	public void onBlockAdded(net.minecraft.world.World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
	}

	public void breakBlock(net.minecraft.world.World world, int x, int y, int z, Block block, int arg6) {
		super.breakBlock(world, x, y, z, block, arg6);
		world.removeTileEntity(x, y, z);
	}

	public boolean onBlockEventReceived(net.minecraft.world.World world, int x, int y, int z, int arg5, int arg6) {
		super.onBlockEventReceived(world, x, y, z, arg5, arg6);
		TileEntity tileentity = world.getTileEntity(x, y, z);
		return tileentity != null ? tileentity.receiveClientEvent(arg5, arg6) : false;
	}

	@Override
	public TileEntity createNewTileEntity(net.minecraft.world.World world, int metadata) {
		return this.createNewTileEntity(new World(world), metadata);
	}

	public TileEntity createNewTileEntity(World world, int metadata) {
		return super.createTileEntity(world.real(), metadata);
	}

}
