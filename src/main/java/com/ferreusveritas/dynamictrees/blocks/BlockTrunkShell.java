package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTrunkShell extends Block {
	
	public static final PropertyEnum<Surround> COREDIR = PropertyEnum.create("coredir", Surround.class);
	
	public static final String name = "trunkshell";
	
	public static class ShellMuse {
		public final IBlockState blockState;
		public final BlockPos pos;
		public final Surround dir;
		
		public ShellMuse(IBlockState blockState, BlockPos pos, Surround dir) {
			this.blockState = blockState;
			this.pos = pos;
			this.dir = dir;
		}
		
		public int getRadius() {
			Block block = blockState.getBlock();
			return block instanceof BlockBranch ? ((BlockBranch)block).getRadius(blockState) : 0;
		}
	}
	
	public BlockTrunkShell() {
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(COREDIR, Surround.S));
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
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
		if(getMuse(worldIn, state, pos) == null) {
			//worldIn.setBlockToAir(pos);
		}
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		ShellMuse muse = getMuse(world, state, pos);
		if(muse != null) {
			return muse.blockState.getBlock().removedByPlayer(muse.blockState, world, muse.pos, player, willHarvest);
		}
		
		return true;
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
		ShellMuse muse = getMuse(world, blockState, pos);
		if(muse != null) {
			return muse.blockState.getBlock().getBlockHardness(muse.blockState, world, muse.pos);
		} else {
			scheduleForClearing(world, pos);
		}
		
		return 0.0f;
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		ShellMuse muse = getMuse(world, pos);
		if(muse != null) {
			return muse.blockState.getBlock().getExplosionResistance(world, muse.pos, exploder, explosion);
		} else {
			scheduleForClearing(world, pos);
		}
		
		return 0.0f;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess access, BlockPos pos) {
		ShellMuse muse = getMuse(access, pos);
		if(muse == null) {
			scheduleForClearing(access, pos);
			return true;
		}
		
		return false;
	}
	
	public Surround getMuseDir(@Nonnull IBlockState blockState, @Nonnull BlockPos pos) {
		return blockState.getValue(COREDIR);
	}
	
	@Nullable
	public ShellMuse getMuse(IBlockAccess world, @Nonnull BlockPos pos) {
		return getMuse(world, world.getBlockState(pos), pos);
	}
	
	@Nullable
	public ShellMuse getMuse(IBlockAccess world, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
		Surround museDir = getMuseDir(state, pos);
		BlockPos musePos = pos.add(museDir.getOffset());
		IBlockState museState = world.getBlockState(musePos);
		Block block = museState.getBlock();
		if(block instanceof IMusable && ((IMusable)block).isMusable()) {
			return new ShellMuse(museState, musePos, museDir);
		}
		
		return null;
	}

	public void scheduleForClearing(IBlockAccess access, BlockPos pos) {
		if(access instanceof World) {
			//((World) access).scheduleBlockUpdate(pos, this, 0, 3);
		}
	}
	
	@Override
	public void onNeighborChange(IBlockAccess access, BlockPos pos, BlockPos neighbor) {
		ShellMuse muse = getMuse(access, pos);
		if(muse == null) {
			scheduleForClearing(access, pos);
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
		ShellMuse muse = getMuse(access, state, pos);
		if(muse != null) {
			AxisAlignedBB aabb = muse.blockState.getBoundingBox(access, muse.pos);
			return aabb.offset(new BlockPos(muse.dir.getOffset())).intersect(FULL_BLOCK_AABB);
		} else {
			scheduleForClearing(access, pos);
			return FULL_BLOCK_AABB;//NULL_AABB;
		}
		
	}
	
	@Override
	public boolean isAir(IBlockState state, IBlockAccess access, BlockPos pos) {
		ShellMuse muse = getMuse(access, state, pos);
		return muse == null;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
}
