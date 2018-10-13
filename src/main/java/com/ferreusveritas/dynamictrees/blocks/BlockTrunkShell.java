package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTrunkShell extends Block {
	
	public static final PropertyEnum<Surround> COREDIR = PropertyEnum.create("coredir", Surround.class);
	
	public static final String name = "trunkshell";
	
	public static class ShellMuse {
		public final IBlockState state;
		public final BlockPos pos;
		public final Surround dir;
		
		public ShellMuse(IBlockState state, BlockPos pos, Surround dir) {
			this.state = state;
			this.pos = pos;
			this.dir = dir;
		}
		
		public int getRadius() {
			Block block = state.getBlock();
			return block instanceof BlockBranch ? ((BlockBranch)block).getRadius(state) : 0;
		}
	}
	
	public BlockTrunkShell() {
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(COREDIR, Surround.S));
		setRegistryName(name);
		setUnlocalizedName(name);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(COREDIR, Surround.values()[meta & 0x07]);
	}
	
	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		return state.getValue(COREDIR).ordinal() & 0x07;
	}
	
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {COREDIR});
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if(getMuseUnchecked(worldIn, state, pos) == null) {
			worldIn.setBlockToAir(pos);
		}
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		ShellMuse muse = getMuse(world, state, pos);
		if(muse != null) {
			return muse.state.getBlock().removedByPlayer(muse.state, world, muse.pos, player, willHarvest);
		}
		
		return true;
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
		ShellMuse muse = getMuse(world, blockState, pos);
		return muse != null ? muse.state.getBlock().getBlockHardness(muse.state, world, muse.pos) : 0.0f;
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		ShellMuse muse = getMuse(world, pos);
		return muse != null ? muse.state.getBlock().getExplosionResistance(world, muse.pos, exploder, explosion) : 0.0f;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess access, BlockPos pos) {
		return getMuse(access, pos) == null;
	}
	
	public Surround getMuseDir(@Nonnull IBlockState state, @Nonnull BlockPos pos) {
		return state.getValue(COREDIR);
	}
	
	@Nullable
	public ShellMuse getMuseUnchecked(@Nonnull IBlockAccess access, @Nonnull BlockPos pos) {
		return getMuseUnchecked(access, access.getBlockState(pos), pos);
	}
	
	@Nullable
	public ShellMuse getMuseUnchecked(@Nonnull IBlockAccess access, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
		Surround museDir = getMuseDir(state, pos);
		BlockPos musePos = pos.add(museDir.getOffset());
		IBlockState museState = access.getBlockState(musePos);
		Block block = museState.getBlock();
		if(block instanceof IMusable && ((IMusable)block).isMusable()) {
			return new ShellMuse(museState, musePos, museDir);
		}
		
		return null;
	}

	@Nullable
	public ShellMuse getMuse(@Nonnull IBlockAccess access, @Nonnull BlockPos pos) {
		return getMuse(access, access.getBlockState(pos), pos);
	}
	
	@Nullable
	public ShellMuse getMuse(@Nonnull IBlockAccess access, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
		ShellMuse muse = getMuseUnchecked(access, state, pos);
		
		//Check the muse for validity
		if(muse == null || muse.getRadius() <= 8) {
			scheduleForClearing(access, pos);
		}
		
		return muse;
	}
	
	public void scheduleForClearing(IBlockAccess access, BlockPos pos) {
		if(access instanceof World) {
			World world = (World) access;
			if(!world.isRemote) {
				world.scheduleBlockUpdate(pos, this, 0, 3);
			}
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		getMuse(world, pos);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		ShellMuse muse = getMuse(access, state, pos);
		if(muse != null) {
			AxisAlignedBB aabb = muse.state.getBoundingBox(access, muse.pos);
			return aabb.offset(new BlockPos(muse.dir.getOffset())).intersect(FULL_BLOCK_AABB);
		} else {
			return new AxisAlignedBB(0, 0, 0, 0, 0, 0);//NULL_AABB;
		}
		
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		AxisAlignedBB aabb = super.getCollisionBoundingBox(blockState, worldIn, pos);
		return aabb == FULL_BLOCK_AABB ? NULL_AABB : aabb;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ShellMuse muse = getMuse(world, state, pos);
		return muse != null ? muse.state.getBlock().getPickBlock(muse.state, target, world, muse.pos, player) : ItemStack.EMPTY;
	}
	
	@Override
	public boolean isAir(IBlockState state, IBlockAccess access, BlockPos pos) {
		return getMuse(access, state, pos) == null;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		ShellMuse muse = getMuse(world, pos);
		if(muse != null) {
			muse.state.getBlock().onBlockExploded(world, muse.pos, explosion);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ShellMuse muse = getMuse(world, pos);
		if(muse != null) {
			return muse.state.getBlock().onBlockActivated(world, muse.pos, muse.state, playerIn, hand, facing, hitX, hitY, hitZ);
		}
		
		return false;
	}
	
	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;//This is the simple solution to the problem.  Maybe I'll work it out later
	}
	
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return 0;//This is the simple solution to the problem.  Maybe I'll work it out later
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
}
