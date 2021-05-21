package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.blocks.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.Null;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class TrunkShellBlock extends BlockWithDynamicHardness implements IWaterLoggable {
	
	public static final EnumProperty<Surround> CORE_DIR = EnumProperty.create("coredir", Surround.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

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
			final Block block = this.state.getBlock();
			return block instanceof BranchBlock ? ((BranchBlock) block).getRadius(state) : 0;
		}
	}

	public TrunkShellBlock() {
		super(Block.Properties.of(Material.WOOD));
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////

	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(CORE_DIR).add(WATERLOGGED);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (this.getMuseUnchecked(worldIn, state, pos) == null) {
			if (state.getValue(WATERLOGGED))
				worldIn.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
			else
				worldIn.removeBlock(pos, false);
		}
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().removedByPlayer(muse.state, world, muse.pos, player, willHarvest, world.getFluidState(pos)), false);
	}

	@Override
	public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
		return Null.applyIfNonnull(this.getMuse(worldIn, state, pos), muse -> muse.state.getBlock().getDestroyProgress(muse.state, player, worldIn, muse.pos), 0f);
	}

	@Override
	public float getHardness(IBlockReader world, BlockPos pos) {
		return Null.applyIfNonnull(this.getMuse(world, pos), muse -> ((BlockWithDynamicHardness) muse.state.getBlock()).getHardness(world, muse.pos), super.getHardness(world, pos));
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getSoundType(muse.state, world, muse.pos, entity), SoundType.WOOD);
	}

	@Override
	public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
		return Null.applyIfNonnull(this.getMuse(world, pos), muse -> muse.state.getBlock().getExplosionResistance(world.getBlockState(pos), world, muse.pos, explosion), 0f);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
		return this.getMuse(useContext.getLevel(), useContext.getClickedPos()) == null;
	}

	public Surround getMuseDir(BlockState state, BlockPos pos) {
		return state.getValue(CORE_DIR);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(IBlockReader access, BlockPos pos) {
		return this.getMuseUnchecked(access, access.getBlockState(pos), pos);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(IBlockReader access, BlockState state, BlockPos pos) {
		return this.getMuseUnchecked(access, state, pos, pos);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(IBlockReader access, BlockState state, BlockPos pos, BlockPos originalPos) {
		final Surround museDir = getMuseDir(state, pos);
		final BlockPos musePos = pos.offset(museDir.getOffset());
		final BlockState museState = CoordUtils.getStateSafe(access, musePos);

		if (museState == null)
			return null;

		final Block block = museState.getBlock();
		if (block instanceof IMusable && ((IMusable) block).isMusable(access, museState, musePos)) {
			return new ShellMuse(museState, musePos, museDir, musePos.subtract(originalPos));
		} else if (block instanceof TrunkShellBlock) { // If its another trunkshell, then this trunkshell is on another layer. IF they share a common direction, we return that shell's muse.
			final Vector3i offset = ((TrunkShellBlock) block).getMuseDir(museState, musePos).getOffset();
			if (new Vector3d(offset.getX(), offset.getY(), offset.getZ()).add(new Vector3d(museDir.getOffset().getX(), museDir.getOffset().getY(), museDir.getOffset().getZ())).lengthSqr() > 2.25){
				return (((TrunkShellBlock) block).getMuseUnchecked(access, museState, musePos, originalPos));
			}
		}
		return null;
	}

	@Nullable
	public ShellMuse getMuse(IBlockReader access, BlockPos pos) {
		return this.getMuse(access, access.getBlockState(pos), pos);
	}

	@Nullable
	public ShellMuse getMuse(IBlockReader access, BlockState state, BlockPos pos) {
		final ShellMuse muse = this.getMuseUnchecked(access, state, pos);

		// Check the muse for validity.
		if (muse == null || muse.getRadius() <= 8) {
			this.scheduleForClearing(access, pos);
		}

		return muse;
	}

	public void scheduleForClearing(IBlockReader access, BlockPos pos) {
		if (!(access instanceof World) || !((World) access).isClientSide())
			return;

		((World) access).getBlockTicks().scheduleTick(pos.immutable(), this, 0, TickPriority.HIGH);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		this.getMuse(world, pos);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean p_220069_6_) {
		this.getMuse(world, pos);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return Null.applyIfNonnull(this.getMuse(reader, state, pos), muse -> VoxelShapes.create(muse.state.getShape(reader, muse.pos).bounds().move(muse.museOffset)), VoxelShapes.empty());
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getPickBlock(muse.state, target, world, muse.pos, player), ItemStack.EMPTY);
	}

	@Override
	public boolean isAir(BlockState state, IBlockReader access, BlockPos pos) {
		return this.getMuse(access, state, pos) == null;
	}

	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		Null.consumeIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onBlockExploded(muse.state, world, muse.pos, explosion));
	}

	//TODO: This may not even be necessary
	@Nullable
	protected Surround findDetachedMuse(World world, BlockPos pos) {
		for (Surround s : Surround.values()) {
			final BlockState state = world.getBlockState(pos.offset(s.getOffset()));

			if (state.getBlock() instanceof IMusable)
				return s;
		}
		return null;
	}

	//TODO: This may not even be necessary
	@Override
	public void destroy(IWorld world, BlockPos pos, BlockState state) {
		final BlockState newState = world.getBlockState(pos);

		if (newState.getBlock() != Blocks.AIR)
			return;

		Null.consumeIfNonnull(this.findDetachedMuse((World) world, pos),
				surround -> world.setBlock(pos, defaultBlockState().setValue(CORE_DIR, surround), 1));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().use(muse.state, world, muse.pos, playerIn, hand, hit), ActionResultType.FAIL);
	}

	@Override
	public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return false; // This is the simple solution to the problem.  Maybe I'll work it out later.
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 0; // This is the simple solution to the problem.  Maybe I'll work it out later.
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		if (state.getBlock() == this) {
			final ShellMuse muse = this.getMuseUnchecked(world, state, pos);

			if (muse == null)
				return true;

			final BlockState museState = muse.state;
			final BlockPos musePos = muse.pos;

			manager.destroy(musePos, museState);
		}
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
		BlockPos shellPos;
		if (target instanceof BlockRayTraceResult) {
			shellPos = ((BlockRayTraceResult) target).getBlockPos();
		} else {
			return false;
		}

		if (state.getBlock() == this) {
			final ShellMuse muse = this.getMuseUnchecked(world, state, shellPos);

			if (muse == null)
				return true;

			final BlockState museState = muse.state;
			final BlockPos musePos = muse.pos;
			final Random rand = world.random;

			int x = musePos.getX();
			int y = musePos.getY();
			int z = musePos.getZ();
			AxisAlignedBB axisalignedbb = museState.getBlockSupportShape(world, musePos).bounds();
			double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
			double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
			double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;

			switch(((BlockRayTraceResult) target).getDirection()) {
				case DOWN:  d1 = y + axisalignedbb.minY - 0.1D; break;
				case UP:    d1 = y + axisalignedbb.maxY + 0.1D; break;
				case NORTH: d2 = z + axisalignedbb.minZ - 0.1D; break;
				case SOUTH: d2 = z + axisalignedbb.maxZ + 0.1D; break;
				case WEST:  d0 = x + axisalignedbb.minX - 0.1D; break;
				case EAST:  d0 = x + axisalignedbb.maxX + 0.1D; break;
			}

			// Safe to spawn particles here since this is a client side only member function.
			world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, museState), d0, d1, d2, 0, 0, 0);
		}

		return true;
	}
	
}
