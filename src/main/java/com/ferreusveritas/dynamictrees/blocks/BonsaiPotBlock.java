package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BonsaiPotBlock extends ContainerBlock {
	
//	public static final MimicProperty POT = new MimicProperty("pot");
		
	public static final String name = "bonsaipot";
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);
	
	public BonsaiPotBlock() {
		this(name);
	}
	
	public BonsaiPotBlock(String name) {
		super(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0));
		setRegistryName(name);
	}
	
	//////////////////////////////
	// SPECIES PROPERTIES
	//////////////////////////////
	
	public Species getSpecies(World access, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = getTileEntityBonsai(access, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getSpecies() : Species.NULL_SPECIES;
	}
	
	public boolean setSpecies(World world, Species species, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = getTileEntityBonsai(world, pos);
		if(bonsaiPotTE != null) {
			bonsaiPotTE.setSpecies(species);
			return true;
		}
		return false;
	}
	
	public BlockState getPotState(World world, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = getTileEntityBonsai(world, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getPot() : Blocks.FLOWER_POT.getDefaultState();
	}
	
	public boolean setPotState(World world, BlockState potState, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = getTileEntityBonsai(world, pos);
		if(bonsaiPotTE != null) {
			bonsaiPotTE.setPot(potState);
			return true;
		}
		return false;
	}
	
	
	///////////////////////////////////////////
	// TILE ENTITY
	///////////////////////////////////////////

	private BonsaiTileEntity getTileEntityBonsai(World access, BlockPos pos) {
		TileEntity tileEntity = access.getTileEntity(pos);
		return tileEntity instanceof BonsaiTileEntity ? (BonsaiTileEntity) tileEntity : null;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new BonsaiTileEntity();
	}
//
//
//	///////////////////////////////////////////
//	// INTERACTION
//	///////////////////////////////////////////
//
//	//Unlike a regular flower pot this is only used to eject the contents
//	@Override
//	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//		ItemStack heldItem = player.getHeldItem(hand);
//
//		if(hand == Hand.MAIN_HAND && heldItem.getItem() == Items.AIR) { //Empty hand
//			Species species = getSpecies(world, pos);
//
//			if(!world.isRemote) {
//				ItemStack seedStack = species.getSeedStack(1);
//				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), seedStack));
//			}
//
//			world.setBlockState(pos, getPotState(world, pos));//Return back to an empty pot
//
//			return true;
//		}
//
//		return false;
//	}
//
//
//	@Override
//	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
//
//		if(target.sideHit == Direction.UP) {
//			Species species = getSpecies(world, pos);
//			if(species != Species.NULLSPECIES) {
//				return species.getSeedStack(1);
//			}
//		}
//
//		BlockState potState = getPotState(world, pos);
//
//		if(potState.getBlock() == Blocks.FLOWER_POT) {
//			return new ItemStack(Items.FLOWER_POT);
//		}
//
//		if(potState.getBlock() instanceof BlockFlowerPot) {
//			return new ItemStack(potState.getBlock(), 1, potState.getBlock().damageDropped(potState));
//		}
//
//		return new ItemStack(Items.FLOWER_POT);
//	}
//
//	/** Get the Item that this Block should drop when harvested. */
//	@Override
//	@Nullable
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return Items.FLOWER_POT;
//	}
//
//	@Override
//	public List<ItemStack> getDrops(IBlockReader world, BlockPos pos, BlockState state, int fortune) {
//		List<ItemStack> ret = super.getDrops(world, pos, state, fortune);//Return the pot itself
//		ret.add(getSpecies(world, pos).getSeedStack(1));//Add the seed in the pot
//		return ret;
//	}
//
//	@Override
//	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
//		if (!world.getBlockState(pos.down()).isSideSolid(world, pos, Direction.UP)) {
//			this.dropBlockAsItem(world, pos, state, 0);
//			world.setBlockToAir(pos);
//		}
//	}
//
//	@Override
//	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
//		if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
//		return super.removedByPlayer(state, world, pos, player, willHarvest);
//	}
//
//	@Override
//	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack tool) {
//		super.harvestBlock(world, player, pos, state, te, tool);
//		world.setBlockToAir(pos);
//	}
//
//	///////////////////////////////////////////
//	// BLOCKSTATES
//	///////////////////////////////////////////
//
//	@Override
//	protected BlockStateContainer createBlockState() {
//		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] {SpeciesProperty.SPECIES, POT});
//	}
//
//	@Override
//	public BlockState getExtendedState(BlockState state, IBlockReader access, BlockPos pos) {
//		return state instanceof BlockState ? ((BlockState)state)
//			.withProperty(SpeciesProperty.SPECIES, getSpecies(access, pos))
//			.withProperty(POT, getPotState(access, pos)) : state;
//	}
//
//	///////////////////////////////////////////
//	// PHYSICAL BOUNDS
//	///////////////////////////////////////////
//
//	@Override
//	public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos) {
//		return FLOWER_POT_AABB;
//	}
//
//	///////////////////////////////////////////
//	// RENDERING
//	///////////////////////////////////////////
//
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public BlockRenderLayer getBlockLayer() {
//		return BlockRenderLayer.CUTOUT;
//	}
//
//	@Override
//	public EnumBlockRenderType getRenderType(BlockState state) {
//		return EnumBlockRenderType.MODEL;
//	}
//
//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return face == Direction.DOWN ? BlockFaceShape.CENTER_SMALL : BlockFaceShape.UNDEFINED;
//	}
//
}
