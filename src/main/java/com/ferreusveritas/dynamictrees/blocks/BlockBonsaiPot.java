package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty.MimicProperty;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBonsaiPot extends BlockContainer {

	public static final MimicProperty SPECIES = new MimicProperty("species");
		
	public static final String name = "bonsaipot";
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public BlockBonsaiPot() {
		this(name);
	}
	
	public BlockBonsaiPot(String name) {
		super(Blocks.FLOWER_POT.getMaterial(Blocks.FLOWER_POT.getDefaultState()));
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	//////////////////////////////
	// SPECIES PROPERTIES
	//////////////////////////////
	
	public Species getSpecies(IBlockAccess access, BlockPos rootPos) {
		TileEntitySpecies rootyDirtTE = getTileEntitySpecies(access, rootPos);
		return rootyDirtTE instanceof TileEntitySpecies ? rootyDirtTE.getSpecies() : Species.NULLSPECIES;
	}
	
	public boolean setSpecies(World world, Species species, BlockPos pos) {
		world.setBlockState(pos, getDefaultState());
		TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, pos);
		if(rootyDirtTE instanceof TileEntitySpecies) {
			rootyDirtTE.setSpecies(species);
			return true;
		}
		return false;
	}
	
	public IBlockState getSaplingState(IBlockAccess access, BlockPos pos) {
		return getSpecies(access, pos).getDynamicSapling();
	}

	
	///////////////////////////////////////////
	// TILE ENTITY
	///////////////////////////////////////////
	
	private TileEntitySpecies getTileEntitySpecies(IBlockAccess access, BlockPos pos) {
		return (TileEntitySpecies) access.getTileEntity(pos);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntitySpecies();
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	//Unlike a regular flower pot this is only used to eject the contents
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		
		if(hand == EnumHand.MAIN_HAND && heldItem.getItem() == ItemBlock.getItemFromBlock(Blocks.AIR)) { //Empty hand
			Species species = getSpecies(world, pos);
			
			if(!world.isRemote) {
				ItemStack seedStack = species.getSeedStack(1);
				ItemStack saplingStack = new ItemStack(species.getTree().getPrimitiveSaplingBlockState().getBlock(), 1, species.getTree().getPrimitiveSaplingBlockState().getValue(BlockSapling.TYPE).getMetadata());
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
		java.util.List<ItemStack> ret = super.getDrops(world, pos, state, fortune);//Return the pot itself
		ret.add(getSpecies(world, pos).getSeedStack(1));//Add the seed in the pot
		return ret;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.getBlockState(pos.down()).isSideSolid(world, pos, EnumFacing.UP)) {
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
    }
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] {SPECIES});
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).withProperty(SPECIES, getSaplingState(access, pos)) : state;
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
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
	}
	
}
