package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityRootyDirt;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRootyDirtTE extends BlockRootyDirt implements ITileEntityProvider {
	
	static String name = "rootydirt_te";
	
	public static final PropertyInteger LIFE = PropertyInteger.create("life", 0, 15);
	public static final PropertyEnum MIMIC = PropertyEnum.create("mimic", EnumMimicType.class);
	
	public BlockRootyDirtTE() {
		this(name);
	}
	
	public BlockRootyDirtTE(String name) {
		super(name);
        this.isBlockContainer = true;
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
    
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		BlockPos treePos = pos.offset(getTrunkDirection(blockAccess, pos));
		return TreeHelper.isBranch(blockAccess, treePos) ? TreeHelper.getBranch(blockAccess, treePos).getTree(blockAccess, treePos) : DynamicTree.NULLTREE;
	}

	/**
	 * Rooty Dirt can report whatever {@link DynamicTree} species it wants to be.  By default we'll just 
	 * make it report whatever {@link DynamicTree} the above {@link BlockBranch} says it is.
	 */
	public Species getSpecies(World world, BlockPos pos) {
		DynamicTree tree = getTree(world, pos);
		TileEntity entity = world.getTileEntity(pos);
		if(tree!= DynamicTree.NULLTREE && entity instanceof TileEntityRootyDirt) {
			TileEntityRootyDirt rootyDirtTE = (TileEntityRootyDirt) entity;
			Species species = rootyDirtTE.getSpecies();
			if(species.getTree() == tree) {//As a sanity check we should see if the tree and the stored species are a match
				return rootyDirtTE.getSpecies();
			}
		}
		return tree.getCommonSpecies();
	}
	
}
