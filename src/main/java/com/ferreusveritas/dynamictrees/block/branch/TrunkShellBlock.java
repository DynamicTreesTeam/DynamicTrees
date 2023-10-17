package com.ferreusveritas.dynamictrees.block.branch;

import com.ferreusveritas.dynamictrees.block.BlockWithDynamicHardness;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
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
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import javax.annotation.Nullable;
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
        super(Properties.of().ignitedByLava().pushReaction(PushReaction.BLOCK));
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    ///////////////////////////////////////////
    // BLOCKSTATE
    ///////////////////////////////////////////

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CORE_DIR).add(WATERLOGGED);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ShellMuse muse = this.getMuseUnchecked(level, state, pos);
        if (!isValid(muse)) {
            if (state.getValue(WATERLOGGED)) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            } else {
                level.removeBlock(pos, false);
            }
        }
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().onDestroyedByPlayer(muse.state, level, muse.pos, player, willHarvest, level.getFluidState(pos)), false);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().getDestroyProgress(muse.state, player, level, muse.pos), 0f);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        return Null.applyIfNonnull(this.getMuse(level, pos), muse -> ((BlockWithDynamicHardness) muse.state.getBlock()).getHardness(state, level, muse.pos), super.getHardness(state, level, pos));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().getSoundType(muse.state, level, muse.pos, entity), SoundType.WOOD);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return Null.applyIfNonnull(this.getMuse(level, pos), muse -> muse.state.getBlock().getExplosionResistance(level.getBlockState(pos), level, muse.pos, explosion), 0f);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        final Level level = useContext.getLevel();
        final BlockPos clickedPos = useContext.getClickedPos();
        if (this.museDoesNotExist(level, state, clickedPos)) {
            this.scheduleUpdateTick(level, clickedPos);
            return false;
        }
        return false;
    }

    public Surround getMuseDir(BlockState state, BlockPos pos) {
        return state.getValue(CORE_DIR);
    }

    public boolean museDoesNotExist(BlockGetter level, BlockState state, BlockPos pos) {
        final BlockPos musePos = pos.offset(this.getMuseDir(state, pos).getOffset());
        return CoordUtils.getStateSafe(level, musePos) == null;
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter level, BlockPos pos) {
        return this.getMuseUnchecked(level, level.getBlockState(pos), pos);
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter level, BlockState state, BlockPos pos) {
        return this.getMuseUnchecked(level, state, pos, pos);
    }

    @Nullable
    public ShellMuse getMuseUnchecked(BlockGetter level, BlockState state, BlockPos pos, BlockPos originalPos) {
        final Surround museDir = getMuseDir(state, pos);
        final BlockPos musePos = pos.offset(museDir.getOffset());
        final BlockState museState = CoordUtils.getStateSafe(level, musePos);

        if (museState == null) {
            return null;
        }

        final Block block = museState.getBlock();
        if (block instanceof Musable && ((Musable) block).isMusable(level, museState, musePos)) {
            return new ShellMuse(museState, musePos, museDir, musePos.subtract(originalPos));
        } else if (block instanceof TrunkShellBlock) { // If its another trunkshell, then this trunkshell is on another layer. IF they share a common direction, we return that shell's muse.
            final Vec3i offset = ((TrunkShellBlock) block).getMuseDir(museState, musePos).getOffset();
            if (new Vec3(offset.getX(), offset.getY(), offset.getZ()).add(new Vec3(museDir.getOffset().getX(), museDir.getOffset().getY(), museDir.getOffset().getZ())).lengthSqr() > 2.25) {
                return (((TrunkShellBlock) block).getMuseUnchecked(level, museState, musePos, originalPos));
            }
        }
        return null;
    }

    @Nullable
    public ShellMuse getMuse(BlockGetter level, BlockPos pos) {
        return this.getMuse(level, level.getBlockState(pos), pos);
    }

    @Nullable
    public ShellMuse getMuse(BlockGetter level, BlockState state, BlockPos pos) {
        final ShellMuse muse = this.getMuseUnchecked(level, state, pos);

        // Check the muse for validity.
        if (!isValid(muse)) {
            this.scheduleUpdateTick(level, pos);
        }

        return muse;
    }

    protected boolean isValid(@Nullable ShellMuse muse) {
        return muse != null && muse.getRadius() > 8;
    }

    public void scheduleUpdateTick(BlockGetter level, BlockPos pos) {
        if (!(level instanceof LevelAccessor)) {
            return;
        }

        ((LevelAccessor) level).getBlockTicks().schedule(new ScheduledTick<Block>(this,pos.immutable(), 0, TickPriority.HIGH,0));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean p_220069_6_) {
        this.scheduleUpdateTick(level, pos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> Shapes.create(muse.state.getShape(level, muse.pos).bounds().move(muse.museOffset)), Shapes.empty());
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().getCloneItemStack(muse.state, target, level, muse.pos, player), ItemStack.EMPTY);
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
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        Null.consumeIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().onBlockExploded(muse.state, level, muse.pos, explosion));
    }

    //TODO: This may not even be necessary
    @Nullable
    protected Surround findDetachedMuse(Level level, BlockPos pos) {
        for (Surround s : Surround.values()) {
            final BlockState state = level.getBlockState(pos.offset(s.getOffset()));

            if (state.getBlock() instanceof Musable) {
                return s;
            }
        }
        return null;
    }

    //TODO: This may not even be necessary
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        final BlockState newState = level.getBlockState(pos);

        if (newState.getBlock() != Blocks.AIR) {
            return;
        }

        Null.consumeIfNonnull(this.findDetachedMuse((Level) level, pos),
                surround -> level.setBlock(pos, defaultBlockState().setValue(CORE_DIR, surround), 1));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        return Null.applyIfNonnull(this.getMuse(level, state, pos), muse -> muse.state.getBlock().use(muse.state, level, muse.pos, playerIn, hand, hit), InteractionResult.FAIL);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return false; // This is the simple solution to the problem.  Maybe I'll work it out later.
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 0; // This is the simple solution to the problem.  Maybe I'll work it out later.
    }

    public boolean isFullBlockShell(BlockGetter level, BlockPos pos) {
        return isFullBlockShell(getMuse(level, pos));
    }

    public boolean isFullBlockShell(@Nullable ShellMuse muse) {
        return muse != null && isFullBlockShell(muse.getRadius());
    }

    public boolean isFullBlockShell(int radius) {
        return (radius - 8) % 16 == 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathType) {
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
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.getFluidTicks().schedule(new ScheduledTick<>(Fluids.WATER,currentPos, Fluids.WATER.getTickDelay(level),0));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        if (isFullBlockShell(level, pos)) {
            return false;
        }
        return SimpleWaterloggedBlock.super.canPlaceLiquid(level, pos, state, fluid);
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
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return false;
    }


    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                BlockPos shellPos;
                if (target instanceof BlockHitResult) {
                    shellPos = ((BlockHitResult) target).getBlockPos();
                } else {
                    return false;
                }

                if (state.getBlock() instanceof TrunkShellBlock) {
                    final ShellMuse muse = ((TrunkShellBlock)state.getBlock()).getMuseUnchecked(level, state, shellPos);

                    if (muse == null) {
                        return true;
                    }

                    final BlockState museState = muse.state;
                    final BlockPos musePos = muse.pos;
                    final RandomSource rand = level.random;

                    int x = musePos.getX();
                    int y = musePos.getY();
                    int z = musePos.getZ();
                    AABB axisalignedbb = museState.getBlockSupportShape(level, musePos).bounds();
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
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, museState), d0, d1, d2, 0, 0, 0);
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
