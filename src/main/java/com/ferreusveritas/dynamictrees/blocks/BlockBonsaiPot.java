package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBonsaiPot extends Block {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
	public static final String name = "bonsaipot";
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public BlockBonsaiPot() {
		this(name);
	}
	
	public BlockBonsaiPot(String name) {
		super(Blocks.FLOWER_POT.getMaterial(Blocks.FLOWER_POT.getDefaultState()));
		setDefaultState(this.blockState.getBaseState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK));
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	public void setupVanillaTree(DynamicTree tree) {
		trees.put(tree.getPrimitiveSapling().getValue(BlockSapling.TYPE).ordinal(), tree);
	}

	//////////////////////////////
	// TREE PROPERTIES
	//////////////////////////////
	
	public DynamicTree getTree(IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getValue(BlockSapling.TYPE).ordinal());
		}
    	return trees.get(0);
	}
	
	public boolean setTree(World world, DynamicTree tree, BlockPos pos) {
		IBlockState primitiveSapling = tree.getPrimitiveSapling();
		if(primitiveSapling.getBlock() == Blocks.SAPLING) {
			BlockPlanks.EnumType woodType = primitiveSapling.getValue(BlockSapling.TYPE);
			world.setBlockState(pos, getDefaultState().withProperty(BlockSapling.TYPE, woodType));
			return true;
		}
		return false;
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if(hand == EnumHand.MAIN_HAND && heldItem == null) { //Empty hand
			DynamicTree tree = getTree(state);
			
			if(!world.isRemote) {
				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				ItemStack saplingStack = new ItemStack(tree.getPrimitiveSapling().getBlock(), 1, tree.getPrimitiveSapling().getValue(BlockSapling.TYPE).getMetadata());
				CompatHelper.spawnEntity(world, new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), player.isSneaking() ? saplingStack : seedStack));
			}

			world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState());

			return true;
		}
		
		return false;
	}

	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Items.FLOWER_POT);
	}
	
	/** Get the Item that this Block should drop when harvested. */
	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.FLOWER_POT;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		java.util.List<ItemStack> ret = super.getDrops(world, pos, state, fortune);
		DynamicTree tree = getTree(state);
		ret.add(tree.getCommonSpecies().getSeedStack(1));
		return ret;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		if (!world.getBlockState(pos.down()).isSideSolid(world, pos, EnumFacing.UP)) {
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
    }
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	/** Convert the given metadata into a BlockState for this Block */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(meta & 0xF));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockSapling.TYPE).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {BlockSapling.TYPE});
	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FLOWER_POT_AABB;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

}
