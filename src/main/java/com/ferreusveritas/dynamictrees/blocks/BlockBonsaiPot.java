package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.renderers.RendererBonsai;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockBonsaiPot extends BlockBackport {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
	public static final String name = "bonsaipot";

	public BlockBonsaiPot() {
		this(name);
	}
	
	public BlockBonsaiPot(String name) {
		super(Blocks.flower_pot.getMaterial());
		setDefaultState(new BlockAndMeta(this, 0));
		setUnlocalizedNameReg(name);
		setRegistryName(name);
		mapTrees();
		setBlockBoundsForItemRender();
	}

	private void mapTrees() {
		for(DynamicTree tree: DynamicTrees.baseTrees) {
			trees.put(tree.getPrimitiveSapling().getMeta(), tree);
		}
	}

	//////////////////////////////
	// TREE PROPERTIES
	//////////////////////////////
	
	public DynamicTree getTree(IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getMeta());
		}
    	return trees.get(0);
	}
	
	public boolean setTree(World world, DynamicTree tree, BlockPos pos) {
		int woodType = tree.getPrimitiveSapling().getMeta();
		getDefaultState().withMeta(woodType).setInWorld(world, pos);
		return true;
	}
		
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	//Unlike a regular flower pot this is only used to eject the contents
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem();
		
		if(heldItem == null) { //Empty hand
			DynamicTree tree = getTree(state);
			
			if(!world.isRemote) {
				ItemStack seedStack = tree.getSeedStack();
				ItemStack saplingStack = tree.getPrimitiveSapling().toItemStack();
				world.spawnEntityInWorld(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), player.isSneaking() ? saplingStack : seedStack));
			}

			new BlockAndMeta(Blocks.flower_pot).setInWorld(world, pos);

			return true;
		}
		
		return false;
	}

	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		return new ItemStack(Items.flower_pot);
	}
	
	/** Get the Item that this Block should drop when harvested. */
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.flower_pot;
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = pos.getBlockState(world);
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		DynamicTree tree = getTree(state);
		ret.add(tree.getSeedStack());
		return ret;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
	if (!World.doesBlockHaveSolidTopSurface(world, pos.getX(), pos.getY() - 1, pos.getZ())) {
            this.dropBlockAsItem(world, pos.getX(), pos.getY(), pos.getZ(), state.getMeta(), 0);
            pos.setBlockToAir(world);
        }
    }
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	public void setBlockBoundsForItemRender() {
		float f = 0.375F;
		float f1 = f / 2.0F;
		this.setBlockBounds(0.5F - f1, 0.0F, 0.5F - f1, 0.5F + f1, f, 0.5F + f1);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public IIcon getIcon(int side, int meta) {
		return Blocks.flower_pot.getIcon(side, meta);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public int getRenderType() {
		return RendererBonsai.id;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}
	

}
