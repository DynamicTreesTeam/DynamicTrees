package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.RootConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class SurfaceRootBlock extends Block implements SimpleWaterloggedBlock {

    public static final int MAX_RADIUS = 8;

    protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, MAX_RADIUS);
    public static final BooleanProperty GROUNDED = BooleanProperty.create("grounded");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Family family;

    public SurfaceRootBlock(Family family) {
        this(Material.WOOD, family);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    public SurfaceRootBlock(Material material, Family family) {
        super(Block.Properties.of(material)
//                .harvestTool(ToolType.AXE)
//                .harvestLevel(0)
                .strength(2.5f, 1.0F)
                .sound(SoundType.WOOD));

        this.family = family;
    }

    public Family getFamily() {
        return family;
    }

    public static class RootConnection {
        public RootConnections.ConnectionLevel level;
        public int radius;

        public RootConnection(RootConnections.ConnectionLevel level, int radius) {
            this.level = level;
            this.radius = radius;
        }

        @Override
        public String toString() {
            return super.toString() + " Level: " + this.level.toString() + " Radius: " + this.radius;
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return this.family.getBranchItem().map(ItemStack::new).orElse(ItemStack.EMPTY);
    }

    ///////////////////////////////////////////
    // BLOCK STATES
    ///////////////////////////////////////////

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, GROUNDED, WATERLOGGED);
    }

    public int getRadius(BlockState blockState) {
        return blockState.getBlock() == this ? blockState.getValue(RADIUS) : 0;
    }

    public int setRadius(LevelAccessor world, BlockPos pos, int radius, int flags) {
        boolean replacingWater = world.getBlockState(pos).getFluidState() == Fluids.WATER.getSource(false);
        world.setBlock(pos, this.getStateForRadius(radius).setValue(WATERLOGGED, replacingWater), flags);
        return radius;
    }

    public BlockState getStateForRadius(int radius) {
        return this.defaultBlockState().setValue(RADIUS, Mth.clamp(radius, 0, getMaxRadius()));
    }

    public int getMaxRadius() {
        return MAX_RADIUS;
    }

    public int getRadialHeight(int radius) {
        return radius * 2;
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
            worldIn.getFluidTicks().schedule(new ScheduledTick<>(Fluids.WATER, currentPos, Fluids.WATER.getTickDelay(worldIn), 1));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    public RootConnections getConnectionData(final BlockAndTintGetter world, final BlockPos pos) {
        final RootConnections connections = new RootConnections();

        for (Direction dir : CoordUtils.HORIZONTALS) {
            final RootConnection connection = this.getSideConnectionRadius(world, pos, dir);

            if (connection == null) {
                continue;
            }

            connections.setRadius(dir, connection.radius);
            connections.setConnectionLevel(dir, connection.level);
        }

        return connections;
    }


    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean connectionMade = false;
        final int thisRadius = getRadius(state);

        VoxelShape shape = Shapes.empty();

        for (Direction dir : CoordUtils.HORIZONTALS) {
            final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

            if (conn == null) {
                continue;
            }

            connectionMade = true;
            final int r = Mth.clamp(conn.radius, 1, thisRadius);
            final double radius = r / 16.0;
            final double radialHeight = getRadialHeight(r) / 16.0;
            final double gap = 0.5 - radius;

            AABB aabb = new AABB(-radius, 0, -radius, radius, radialHeight, radius);
            aabb = aabb.expandTowards(dir.getStepX() * gap, 0, dir.getStepZ() * gap).move(0.5, 0.0, 0.5);
            shape = Shapes.joinUnoptimized(shape, Shapes.create(aabb), BooleanOp.OR);
        }

        if (!connectionMade) {
            double radius = thisRadius / 16.0;
            double radialHeight = getRadialHeight(thisRadius) / 16.0;
            AABB aabb = new AABB(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, radialHeight, 0.5 + radius);
            shape = Shapes.joinUnoptimized(shape, Shapes.create(aabb), BooleanOp.OR);
        }

        return shape;
    }

    private boolean isAirOrWater (BlockState state){
        return state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER;
    }

    @Nullable
    protected RootConnection getSideConnectionRadius(BlockGetter blockReader, BlockPos pos, Direction side) {
        if (!side.getAxis().isHorizontal()) {
            return null;
        }

        BlockPos dPos = pos.relative(side);
        BlockState state = CoordUtils.getStateSafe(blockReader, dPos);
        final BlockState upState = CoordUtils.getStateSafe(blockReader, pos.above());

        final RootConnections.ConnectionLevel level = (upState != null && isAirOrWater(upState) && state != null && state.isRedstoneConductor(blockReader, dPos)) ?
                RootConnections.ConnectionLevel.HIGH : (state != null && isAirOrWater(state) ? RootConnections.ConnectionLevel.LOW : RootConnections.ConnectionLevel.MID);

        if (level != RootConnections.ConnectionLevel.MID) {
            dPos = dPos.above(level.getYOffset());
            state = CoordUtils.getStateSafe(blockReader, dPos);
        }

        if (state != null && state.getBlock() instanceof SurfaceRootBlock) {
            return new RootConnection(level, ((SurfaceRootBlock) state.getBlock()).getRadius(state));
        } else if (level == RootConnections.ConnectionLevel.MID && TreeHelper.isBranch(state) && TreeHelper.getTreePart(state).getRadius(state) >= 8) {
            return new RootConnection(RootConnections.ConnectionLevel.MID, 8);
        }

        return null;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        final BlockState upstate = world.getBlockState(pos.above());

        if (upstate.getBlock() instanceof TrunkShellBlock) {
            world.setBlockAndUpdate(pos, upstate);
        }

        for (Direction dir : CoordUtils.HORIZONTALS) {
            final BlockPos dPos = pos.relative(dir).below();
            world.getBlockState(dPos).neighborChanged(world, dPos, this, pos, false);
        }

        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!canBlockStay(world, pos, state)) {
            world.removeBlock(pos, false);
        }
    }

    protected boolean canBlockStay(Level world, BlockPos pos, BlockState state) {
        final BlockPos below = pos.below();
        final BlockState belowState = world.getBlockState(below);

        final int radius = getRadius(state);

        if (belowState.isRedstoneConductor(world, below)) { // If a root is sitting on a solid block.
            for (Direction dir : CoordUtils.HORIZONTALS) {
                final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

                if (conn != null && conn.radius > radius) {
                    return true;
                }
            }
        } else { // If the root has no solid block under it.
            boolean connections = false;

            for (Direction dir : CoordUtils.HORIZONTALS) {
                final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

                if (conn == null) {
                    continue;
                }

                if (conn.level == RootConnections.ConnectionLevel.MID) {
                    return false;
                }

                if (conn.radius > radius) {
                    connections = true;
                }
            }

            return connections;
        }

        return false;
    }

}
