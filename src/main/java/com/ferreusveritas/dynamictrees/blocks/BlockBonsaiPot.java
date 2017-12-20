package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.renderers.RendererBonsai;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;

public class BlockBonsaiPot extends BlockBackport {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
	public static final String name = "bonsaipot";

	public BlockBonsaiPot() {
		this(name);
	}
	
	public BlockBonsaiPot(String name) {
		super(Blocks.flower_pot.getMaterial());
		setDefaultState(new BlockState(this, 0));
		setUnlocalizedNameReg(name);
		setRegistryName(name);
		setBlockBoundsForItemRender();
	}

	public void setupVanillaTree(DynamicTree tree) {
		trees.put(tree.getPrimitiveSapling().getMeta(), tree);
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
		world.setBlockState(pos, getDefaultState().withMeta(woodType));
		return true;
	}
	
	public boolean setSpecies(World world, Species species, BlockPos pos) {
		if(species == species.getTree().getCommonSpecies()) {//Make sure the seed is a common species
			return setTree(world, species.getTree(), pos);
		}
		return false;
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	//Unlike a regular flower pot this is only used to eject the contents
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if(hand == EnumHand.MAIN_HAND && heldItem == null) { //Empty hand
			DynamicTree tree = getTree(state);
			
			if(!world.isRemote()) {
				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				ItemStack saplingStack = tree.getPrimitiveSapling().toItemStack();
				CompatHelper.spawnEntity(world, new EntityItem(world.real(), pos.getX(), pos.getY(), pos.getZ(), player.isSneaking() ? saplingStack : seedStack));
			}

			world.setBlockState(pos, new BlockState(Blocks.flower_pot));

			return true;
		}
		
		return false;
	}

	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, net.minecraft.world.World world, int x, int y, int z, EntityPlayer player) {
		return new ItemStack(Items.flower_pot);
	}
	
	/** Get the Item that this Block should drop when harvested. */
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.flower_pot;
	}


	@Override
	public ArrayList<ItemStack> getDrops(World world, BlockPos pos, int metadata, int fortune) {
		IBlockState state = world.getBlockState(pos);
		ArrayList<ItemStack> ret = super.getDrops(world, pos, metadata, fortune);
		DynamicTree tree = getTree(state);
		ret.add(tree.getCommonSpecies().getSeedStack(1));
		return ret;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		if (!World.doesBlockHaveSolidTopSurface(world, pos.down())) {
			this.dropBlockAsItem(world, pos, state.getMeta(), 0);
			world.setBlockToAir(pos);
		}
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	//1.7.10 Does not have blockstates

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
