package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.blocks.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.Null;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IBlockRenderProperties;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class TrunkShellBlock extends BlockWithDynamicHardness implements SimpleWaterloggedBlock {

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

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CORE_DIR).add(WATERLOGGED);
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
        ShellMuse muse = this.getMuseUnchecked(worldIn, state, pos);
        if (!isValid(muse)) {
            if (state.getValue(WATERLOGGED)) {
                worldIn.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            } else {
                worldIn.removeBlock(pos, false);
            }
        }
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onDestroyedByPlayer(muse.state, world, muse.pos, player, willHarvest, world.getFluidState(pos)), false);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        return Null.applyIfNonnull(this.getMuse(worldIn, state, pos), muse -> muse.state.getBlock().getDestroyProgress(muse.state, player, worldIn, muse.pos), 0f);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter world, BlockPos pos) {
        return Null.applyIfNonnull(this.getMuse(world, pos), muse -> ((BlockWithDynamicHardness) muse.state.getBlock()).getHardness(state, world, muse.pos), super.getHardness(state, world, pos));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getSoundType(muse.state, world, muse.pos, entity), SoundType.WOOD);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return Null.applyIfNonnull(this.getMuse(world, pos), muse -> muse.state.getBlock().getExplosionResistance(world.getBlockState(pos), world, muse.pos, explosion), 0f);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        final Level world = useContext.getLevel();
        final BlockPos clickedPos = useContext.getClickedPos();
        if (this.museDoesNotExist(world, state, clickedPos)) {
            this.scheduleUpdateTick(world, clickedPos);
            return false;
        }
        return false;
    }

    public Surround getMuseDir(BlockState state, BlockPos pos) {
        return state.getValue(CORE_DIR);
    }

    public boolean museDoesNotExist(BlockGetter world, BlockState state, BlockPos pos) {
        final BlockPos musePos = pos.offset(this.getMuseDir(state, pos).getOffset());
        return CoordUtils.getStateSafe(world, musePos) == null;
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter access, BlockPos pos) {
        return this.getMuseUnchecked(access, access.getBlockState(pos), pos);
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter access, BlockState state, BlockPos pos) {
        return this.getMuseUnchecked(access, state, pos, pos);
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter access, BlockState state, BlockPos pos, BlockPos originalPos) {
        final Surround museDir = getMuseDir(state, pos);
        final BlockPos musePos = pos.offset(museDir.getOffset());
        final BlockState museState = CoordUtils.getStateSafe(access, musePos);

        if (museState == null) {
            return null;
        }

        final Block block = museState.getBlock();
        if (block instanceof Musable && ((Musable) block).isMusable(access, museState, musePos)) {
            return new ShellMuse(museState, musePos, museDir, musePos.subtract(originalPos));
        } else if (block instanceof TrunkShellBlock) { // If its another trunkshell, then this trunkshell is on another layer. IF they share a common direction, we return that shell's muse.
            final Vec3i offset = ((TrunkShellBlock) block).getMuseDir(museState, musePos).getOffset();
            if (new Vec3(offset.getX(), offset.getY(), offset.getZ()).add(new Vec3(museDir.getOffset().getX(), museDir.getOffset().getY(), museDir.getOffset().getZ())).lengthSqr() > 2.25) {
                return (((TrunkShellBlock) block).getMuseUnchecked(access, museState, musePos, originalPos));
            }
        }
        return null;
    }

    @Nullable
    public ShellMuse getMuse(BlockGetter access, BlockPos pos) {
        return this.getMuse(access, access.getBlockState(pos), pos);
    }

    @Nullable
    public ShellMuse getMuse(BlockGetter access, BlockState state, BlockPos pos) {
        final ShellMuse muse = this.getMuseUnchecked(access, state, pos);

        // Check the muse for validity.
        if (!isValid(muse)) {
            this.scheduleUpdateTick(access, pos);
        }

        return muse;
    }

    protected boolean isValid(@Nullable ShellMuse muse) {
        return muse != null && muse.getRadius() > 8;
    }

    public void scheduleUpdateTick(BlockGetter access, BlockPos pos) {
        if (!(access instanceof LevelAccessor)) {
            return;
        }

        ((LevelAccessor) access).getBlockTicks().schedule(new ScheduledTick<Block>(this,pos.immutable(), 0, TickPriority.HIGH,0));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean p_220069_6_) {
        this.scheduleUpdateTick(world, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return Null.applyIfNonnull(this.getMuse(reader, state, pos), muse -> Shapes.create(muse.state.getShape(reader, muse.pos).bounds().move(muse.museOffset)), Shapes.empty());
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getCloneItemStack(muse.state, target, world, muse.pos, player), ItemStack.EMPTY);
    }

//    @Override
//    protected boolean isAir(BlockState state) {
//        if (this.museDoesNotExist(access, state, state.getBl)) {
//            this.scheduleUpdateTick(access, pos);
//            return false;
//        }
//        return false;
//    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        Null.consumeIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onBlockExploded(muse.state, world, muse.pos, explosion));
    }

    //TODO: This may not even be necessary
    @Nullable
    protected Surround findDetachedMuse(Level world, BlockPos pos) {
        for (Surround s : Surround.values()) {
            final BlockState state = world.getBlockState(pos.offset(s.getOffset()));

            if (state.getBlock() instanceof Musable) {
                return s;
            }
        }
        return null;
    }

    //TODO: This may not even be necessary
    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        final BlockState newState = world.getBlockState(pos);

        if (newState.getBlock() != Blocks.AIR) {
            return;
        }

        Null.consumeIfNonnull(this.findDetachedMuse((Level) world, pos),
                surround -> world.setBlock(pos, defaultBlockState().setValue(CORE_DIR, surround), 1));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().use(muse.state, world, muse.pos, playerIn, hand, hit), InteractionResult.FAIL);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return false; // This is the simple solution to the problem.  Maybe I'll work it out later.
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 0; // This is the simple solution to the problem.  Maybe I'll work it out later.
    }

    public boolean isFullBlockShell(BlockGetter world, BlockPos pos) {
        return isFullBlockShell(getMuse(world, pos));
    }

    public boolean isFullBlockShell(@Nullable ShellMuse muse) {
        return muse != null && isFullBlockShell(muse.getRadius());
    }

    public boolean isFullBlockShell(int radius) {
        return (radius - 8) % 16 == 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType pathType) {
        return false;
    }

    ///////////////////////////////////////////
    // WATER LOGGING
    ///////////////////////////////////////////

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getFluidTicks().schedule(new ScheduledTick<>(Fluids.WATER,currentPos, Fluids.WATER.getTickDelay(worldIn),0));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        if (isFullBlockShell(world, pos)) {
            return false;
        }
        return SimpleWaterloggedBlock.super.canPlaceLiquid(world, pos, state, fluid);
    }

    protected boolean isWaterLogged(BlockState state) {
        return state.hasProperty(WATERLOGGED) && state.getValue(WATERLOGGED);
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return false;
    }


    @Override
    public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
        consumer.accept(new IBlockRenderProperties() {
            @Override
            public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
                BlockPos shellPos;
                if (target instanceof BlockHitResult) {
                    shellPos = ((BlockHitResult) target).getBlockPos();
                } else {
                    return false;
                }

                if (state.getBlock() instanceof TrunkShellBlock) {
                    final ShellMuse muse = ((TrunkShellBlock)state.getBlock()).getMuseUnchecked(world, state, shellPos);

                    if (muse == null) {
                        return true;
                    }

                    final BlockState museState = muse.state;
                    final BlockPos musePos = muse.pos;
                    final Random rand = world.random;

                    int x = musePos.getX();
                    int y = musePos.getY();
                    int z = musePos.getZ();
                    AABB axisalignedbb = museState.getBlockSupportShape(world, musePos).bounds();
                    double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
                    double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
                    double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;

                    switch (((BlockHitResult) target).getDirection()) {
                        case DOWN:
                            d1 = y + axisalignedbb.minY - 0.1D;
                            break;
                        case UP:
                            d1 = y + axisalignedbb.maxY + 0.1D;
                            break;
                        case NORTH:
                            d2 = z + axisalignedbb.minZ - 0.1D;
                            break;
                        case SOUTH:
                            d2 = z + axisalignedbb.maxZ + 0.1D;
                            break;
                        case WEST:
                            d0 = x + axisalignedbb.minX - 0.1D;
                            break;
                        case EAST:
                            d0 = x + axisalignedbb.maxX + 0.1D;
                            break;
                    }

                    // Safe to spawn particles here since this is a client side only member function.
                    world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, museState), d0, d1, d2, 0, 0, 0);
                }

                return true;
            }

            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                if (state.getBlock() instanceof TrunkShellBlock) {
                    final ShellMuse muse = ((TrunkShellBlock)state.getBlock()).getMuseUnchecked(level, state, pos);

                    if (muse == null) {
                        return true;
                    }

                    final BlockState museState = muse.state;
                    final BlockPos musePos = muse.pos;

                    manager.destroy(musePos, museState);
                }
                return true;
            }
        });
    }

}
