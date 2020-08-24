package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockTrunkShell extends Block {
	
	public static final EnumProperty<Surround> COREDIR = EnumProperty.create("coredir", Surround.class);
	
	public static final String defaultName = "trunkshell";
	
	public static class ShellMuse {
		public final BlockState state;
		public final BlockPos pos;
		public final BlockPos museOffset;
		public final Surround dir;
		
		public ShellMuse(BlockState state, BlockPos pos, Surround dir, BlockPos museOffset) {
			this.state = state;
			this.pos = pos;
			this.dir = dir;
			this.museOffset = museOffset;
		}
		
		public int getRadius() {
			Block block = state.getBlock();
			return block instanceof BlockBranch ? ((BlockBranch)block).getRadius(state) : 0;
		}
	}

	public BlockTrunkShell(String name) {
		super(Block.Properties.create(Material.WOOD));
		//setCreativeTab;
		setRegistryName(name);
	}

	public BlockTrunkShell() {
		this(defaultName);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(COREDIR);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if(getMuseUnchecked(worldIn, state, pos) == null) {
			worldIn.removeBlock(pos, false);
		}
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////


	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
		ShellMuse muse = getMuse(world, state, pos);
		if(muse != null) {
			return muse.state.getBlock().removedByPlayer(muse.state, world, muse.pos, player, willHarvest, world.getFluidState(pos));
		}

		return false;
	}

	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader world, BlockPos pos) {
		ShellMuse muse = getMuse(world, blockState, pos);
		return muse != null ? muse.state.getBlock().getBlockHardness(muse.state, world, muse.pos) : 0.0f;
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		ShellMuse muse = getMuse(world, state, pos);
		return muse != null ? muse.state.getBlock().getSoundType(muse.state, world, muse.pos, entity) : SoundType.WOOD;
	}

	@Override
	public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		ShellMuse muse = getMuse(world, pos);
		return muse != null ? muse.state.getBlock().getExplosionResistance(world.getBlockState(pos), world, muse.pos, exploder, explosion) : 0.0f;
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return getMuse(useContext.getWorld(), useContext.getPos()) == null;
	}

	public Surround getMuseDir(@Nonnull BlockState state, @Nonnull BlockPos pos) {
		return state.get(COREDIR);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(@Nonnull IBlockReader access, @Nonnull BlockPos pos) {
		return getMuseUnchecked(access, access.getBlockState(pos), pos);
	}
	@Nullable
	public ShellMuse getMuseUnchecked(@Nonnull IBlockReader access, @Nonnull BlockState state, @Nonnull BlockPos pos) {
		return getMuseUnchecked(access, state, pos, pos);
	}
	@Nullable
	public ShellMuse getMuseUnchecked(@Nonnull IBlockReader access, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull BlockPos originalPos) {
		Surround museDir = getMuseDir(state, pos);
		BlockPos musePos = pos.add(museDir.getOffset());
		BlockState museState = access.getBlockState(musePos);
		Block block = museState.getBlock();
		if(block instanceof IMusable && ((IMusable)block).isMusable()) {
			return new ShellMuse(museState, musePos, museDir, musePos.subtract(originalPos));
		} else if (block instanceof BlockTrunkShell){ //If its another trunkshell, then this trunkshell is on another layer. IF they share a common direction, we return that shell's muse
			if (new Vec3d(((BlockTrunkShell)block).getMuseDir(museState, musePos).getOffset()).add(new Vec3d(museDir.getOffset())).lengthSquared() > 2.25){
				return (((BlockTrunkShell)block).getMuseUnchecked(access, museState, musePos, originalPos));
			}
		}
		return null;
	}

	@Nullable
	public ShellMuse getMuse(@Nonnull IBlockReader access, @Nonnull BlockPos pos) {
		return getMuse(access, access.getBlockState(pos), pos);
	}

	@Nullable
	public ShellMuse getMuse(@Nonnull IBlockReader access, @Nonnull BlockState state, @Nonnull BlockPos pos) {
		ShellMuse muse = getMuseUnchecked(access, state, pos);

		//Check the muse for validity
		if(muse == null || muse.getRadius() <= 8) {
			scheduleForClearing(access, pos);
		}

		return muse;
	}

	public void scheduleForClearing(IBlockReader access, BlockPos pos) {
		if(access instanceof World) {
			World world = (World) access;
			if(!world.isRemote) {
				world.getPendingBlockTicks().scheduleTick(pos.toImmutable(), this, 0, TickPriority.HIGH);
			}
		}
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		getMuse(world, pos);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		ShellMuse muse = getMuse(reader, state, pos);
		if (muse != null){
			VoxelShape shape = muse.state.getShape(reader, muse.pos);
			return VoxelShapes.create(shape.getBoundingBox().offset(muse.museOffset));
		} else {
			return VoxelShapes.empty();//NULL_AABB;
		}
	}

	@Override
	public VoxelShape getCollisionShape(@Nonnull BlockState state,@Nonnull  IBlockReader reader,@Nonnull  BlockPos pos, ISelectionContext context) {
		ShellMuse muse = getMuse(reader, state, pos);
		if(muse != null) {
			VoxelShape shape = muse.state.getShape(reader, muse.pos);
			return VoxelShapes.create(shape.getBoundingBox().offset(muse.museOffset).intersect(VoxelShapes.fullCube().getBoundingBox()));
		} else {
			return VoxelShapes.empty();//NULL_AABB;
		}
	}

	//	@Override
//	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//		return super.getCollisionShape(state, worldIn, pos, context);
//	}

	//	@Override
//	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
//		AxisAlignedBB aabb = super.getCollisionBoundingBox(blockState, worldIn, pos);
//		return aabb == FULL_BLOCK_AABB ? NULL_AABB : aabb;
//	}

//	@Override
//	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
//		if(entityIn instanceof EntityFallingTree) {
//			return;
//		}
//		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
//	}

//	@Override
//	@OnlyIn(Dist.CLIENT)
//    public AxisAlignedBB getSelectedBoundingBox(BlockState state, World worldIn, BlockPos pos) {
//		ShellMuse muse = this.getMuseUnchecked(worldIn, state, pos);
//        return muse.state.getCollisionShape(worldIn, muse.pos).getBoundingBox().offset(muse.pos);
//		//return state.getBoundingBox(worldIn, pos).offset(pos);
//    }


	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ShellMuse muse = getMuse(world, state, pos);
		return muse != null ? muse.state.getBlock().getPickBlock(muse.state, target, world, muse.pos, player) : ItemStack.EMPTY;
	}

	@Override
	public boolean isAir(BlockState state, IBlockReader access, BlockPos pos) {
		return getMuse(access, state, pos) == null;
	}

//	@Override
//	public EnumBlockRenderType getRenderType(BlockState state) {
//		return EnumBlockRenderType.INVISIBLE;
//	}


	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		ShellMuse muse = getMuse(world, pos);
		if(muse != null) {
			muse.state.getBlock().onBlockExploded(muse.state, world, muse.pos, explosion);
		}
	}

	//TODO: This may not even be necessary
	protected Surround findDetachedMuse(World world, BlockPos pos) {
		for(Surround s: Surround.values()) {
			BlockState state = world.getBlockState(pos.add(s.getOffset()));
			if(state.getBlock() instanceof IMusable) {
				return s;
			}
		}
		return null;
	}

	//TODO: This may not even be necessary
	@Override
	public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState state) {
		BlockState newState = world.getBlockState(pos);
		if(newState.getBlock() == Blocks.AIR) {
			Surround surr = findDetachedMuse((World) world, pos);
			if(surr != null) {
				world.setBlockState(pos, getDefaultState().with(COREDIR, surr), 1);
			}
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		ShellMuse muse = getMuse(world, pos);
		if(muse != null) {
			return muse.state.getBlock().onBlockActivated(muse.state, world, muse.pos, playerIn, hand, hit);
		}

		return false;
	}

	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return false;
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 0;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}

//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.UNDEFINED;
//	}


	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return null;
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		//BlockState state = world.getBlockState(pos);
		if (state.getBlock() == this) {
			ShellMuse muse = this.getMuseUnchecked(world, state, pos);
			if (muse == null) return true;

			BlockState museState = muse.state;
			BlockPos musePos = muse.pos;

			manager.addBlockDestroyEffects(musePos, museState);
		}
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
		BlockPos shellPos;
		if (target instanceof BlockRayTraceResult){
			shellPos = ((BlockRayTraceResult)target).getPos();
		} else {
			return false;
		}

		if (state.getBlock() == this) {
			ShellMuse muse = this.getMuseUnchecked(world, state, shellPos);
			if (muse == null) return true;

			BlockState museState = muse.state;
			BlockPos musePos = muse.pos;
			Random rand = world.rand;

			int x = musePos.getX();
			int y = musePos.getY();
			int z = musePos.getZ();
			AxisAlignedBB axisalignedbb = museState.getCollisionShape(world, musePos).getBoundingBox();
			double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
			double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
			double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;

			switch(((BlockRayTraceResult) target).getFace()) {
				case DOWN:  d1 = y + axisalignedbb.minY - 0.1D; break;
				case UP:    d1 = y + axisalignedbb.maxY + 0.1D; break;
				case NORTH: d2 = z + axisalignedbb.minZ - 0.1D; break;
				case SOUTH: d2 = z + axisalignedbb.maxZ + 0.1D; break;
				case WEST:  d0 = x + axisalignedbb.minX - 0.1D; break;
				case EAST:  d0 = x + axisalignedbb.maxX + 0.1D; break;
			}

			// Safe to spawn particles here since this is a client side only member function
			world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, museState), d0, d1, d2, 0, 0, 0);
		}

		return true;
	}
	
}
