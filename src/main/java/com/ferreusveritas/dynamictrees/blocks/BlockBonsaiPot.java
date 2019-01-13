package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.tileentity.TileEntityBonsai;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
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
	
	public static final MimicProperty POT = new MimicProperty("pot");
		
	public static final String name = "bonsaipot";
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);
	
	public BlockBonsaiPot() {
		this(name);
	}
	
	public BlockBonsaiPot(String name) {
		super(Blocks.FLOWER_POT.getMaterial(Blocks.FLOWER_POT.getDefaultState()));
		setRegistryName(name);
		setUnlocalizedName(getRegistryName().toString());
	}
	
	//////////////////////////////
	// SPECIES PROPERTIES
	//////////////////////////////
	
	public Species getSpecies(IBlockAccess access, BlockPos pos) {
		TileEntityBonsai bonsaiPotTE = getTileEntityBonsai(access, pos);
		return bonsaiPotTE instanceof TileEntityBonsai ? bonsaiPotTE.getSpecies() : Species.NULLSPECIES;
	}
	
	public boolean setSpecies(World world, Species species, BlockPos pos) {
		TileEntityBonsai bonsaiPotTE = getTileEntityBonsai(world, pos);
		if(bonsaiPotTE instanceof TileEntityBonsai) {
			bonsaiPotTE.setSpecies(species);
			return true;
		}
		return false;
	}
	
	public IBlockState getPotState(IBlockAccess access, BlockPos pos) {
		TileEntityBonsai bonsaiPotTE = getTileEntityBonsai(access, pos);
		return bonsaiPotTE instanceof TileEntityBonsai ? bonsaiPotTE.getPot() : Blocks.FLOWER_POT.getDefaultState();
	}
	
	public boolean setPotState(World world, IBlockState potState, BlockPos pos) {
		TileEntityBonsai bonsaiPotTE = getTileEntityBonsai(world, pos);
		if(bonsaiPotTE instanceof TileEntityBonsai) {
			bonsaiPotTE.setPot(potState);
			return true;
		}
		return false;
	}
	
	
	///////////////////////////////////////////
	// TILE ENTITY
	///////////////////////////////////////////
	
	private TileEntityBonsai getTileEntityBonsai(IBlockAccess access, BlockPos pos) {
		TileEntity tileEntity = access.getTileEntity(pos);
		return tileEntity instanceof TileEntityBonsai ? (TileEntityBonsai) tileEntity : null;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBonsai();
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
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), seedStack));
			}

			world.setBlockState(pos, getPotState(world, pos));//Return back to an empty pot

			return true;
		}
		
		return false;
	}
	
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		
		if(target.sideHit == EnumFacing.UP) {
			Species species = getSpecies(world, pos);
			if(species != Species.NULLSPECIES) {
				return species.getSeedStack(1);
			}
		}
		
		IBlockState potState = getPotState(world, pos);
		
		if(potState.getBlock() == Blocks.FLOWER_POT) {
			return new ItemStack(Items.FLOWER_POT);			
		}
		
		if(potState.getBlock() instanceof BlockFlowerPot) {
			return new ItemStack(potState.getBlock(), 1, potState.getBlock().damageDropped(potState));
		}
		
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
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] {SpeciesProperty.SPECIES, POT});
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state)
			.withProperty(SpeciesProperty.SPECIES, getSpecies(access, pos))
			.withProperty(POT, getPotState(access, pos)) : state;
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
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN ? BlockFaceShape.CENTER_SMALL : BlockFaceShape.UNDEFINED;
	}
	
}
