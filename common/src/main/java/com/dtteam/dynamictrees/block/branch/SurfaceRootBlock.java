package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.api.network.RootConnections;
import com.dtteam.dynamictrees.api.treedata.SurfaceRootShapeState;
import com.dtteam.dynamictrees.tree.ChunkTreeHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SurfaceRootBlock extends Block implements SimpleWaterloggedBlock {

    public static final int MAX_RADIUS = 8;

    protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, MAX_RADIUS);
    public static final BooleanProperty GROUNDED = BooleanProperty.create("grounded");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Family family;

    protected static final VoxelShape[] shapeCache = new VoxelShape[SurfaceRootShapeState.TOTAL_STATES];

    public SurfaceRootBlock(Identifier id, Family family, Properties properties) {
        super(properties.strength(2.5f, 1.0F).setId(ResourceKey.create(Registries.BLOCK, id)));

        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
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
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
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

    public int setRadius(LevelAccessor level, BlockPos pos, int radius, int flags) {
        boolean replacingWater = level.getBlockState(pos).getFluidState() == Fluids.WATER.getSource(false);
        level.setBlock(pos, this.getStateForRadius(radius).setValue(WATERLOGGED, replacingWater), flags);
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
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    public RootConnections getConnectionData(final BlockAndTintGetter level, final BlockPos pos) {
        final RootConnections connections = new RootConnections();

        for (Direction dir : CoordUtils.HORIZONTALS) {
            final RootConnection connection = this.getSideConnectionRadius(level, pos, dir);

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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final int thisRadius = getRadius(state);

        byte[] radii = new byte[5];
        boolean connectionMade = false;

        radii[4] = (byte)thisRadius; //last radius is the core

        for (Direction dir : CoordUtils.HORIZONTALS) {
            final RootConnection conn = this.getSideConnectionRadius(level, pos, dir);
            if (conn != null) {
                radii[dir.get2DDataValue()] = (byte)Mth.clamp(conn.radius, 1, thisRadius);
                connectionMade = true;
            }
        }

        int index = SurfaceRootShapeState.fromArray(radii).toIndex();

        VoxelShape cached = shapeCache[index];
        if (cached != null) {
            return cached;
        }

        VoxelShape newShape = generateNewRootShape(radii, connectionMade);
        shapeCache[index] = newShape;
        return newShape;
    }

    private VoxelShape generateNewRootShape(byte[] radii, boolean connectionMade) {
        VoxelShape shape = Shapes.empty();
        int thisRadius = radii[4];

        for (Direction dir : CoordUtils.HORIZONTALS) {
            int r = radii[dir.get2DDataValue()];
            if (r == 0) continue;

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
    protected RootConnection getSideConnectionRadius(BlockGetter level, BlockPos pos, Direction side) {
        if (!side.getAxis().isHorizontal()) {
            return null;
        }

        BlockPos dPos = pos.relative(side);
        BlockState state = ChunkTreeHelper.getStateSafe(level, dPos);
        final BlockState upState = ChunkTreeHelper.getStateSafe(level, pos.above());

        if (state == null || upState == null) return null;

        if (TreeHelper.isBranch(state)) {
            int radius = TreeHelper.getTreePart(state).getRadius(state);
            if (radius >= 8)
                return new RootConnection(RootConnections.ConnectionLevel.MID, 8);
        }

        boolean goUp = isAirOrWater(upState) && state.isCollisionShapeFullBlock(level, dPos);
        boolean goDown = isAirOrWater(state);

        final RootConnections.ConnectionLevel connectionLevel;
        if (goUp){
            connectionLevel = RootConnections.ConnectionLevel.HIGH;
        } else if (goDown){
            connectionLevel = RootConnections.ConnectionLevel.LOW;
        } else {
            connectionLevel = RootConnections.ConnectionLevel.MID;
        }

        if (connectionLevel != RootConnections.ConnectionLevel.MID) {
            dPos = dPos.above(connectionLevel.getYOffset());
            state = ChunkTreeHelper.getStateSafe(level, dPos);
        }

        if (state != null && state.getBlock() instanceof SurfaceRootBlock sideRoot) {
            return new RootConnection(connectionLevel,  sideRoot.getRadius(state));
        }

        return null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        level.scheduleTick(pos, state.getBlock(), 0);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (!canBlockStay(level, pos, state)) {
            int thisRad = state.hasProperty(RADIUS) ? state.getValue(RADIUS) : 0;
            level.removeBlock(pos, false);
            for (Direction dir : CoordUtils.HORIZONTALS) {
                updateDisconnectedRoot(level, pos, dir, thisRad);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState destroyed = super.playerWillDestroy(level, pos, state, player);
        int thisRad = state.hasProperty(RADIUS) ? state.getValue(RADIUS) : 0;
        for (Direction dir : CoordUtils.HORIZONTALS) {
            updateDisconnectedRoot(level, pos, dir, thisRad);
        }
        return destroyed;
    }

    private void updateDisconnectedRoot(Level level, BlockPos pos, Direction dir, int thisRad) {
        final RootConnection conn = this.getSideConnectionRadius(level, pos, dir);
        if (conn == null) {
            return;
        }
        if (conn.radius < thisRad){
            BlockPos offsetPos = pos.relative(dir).above(conn.level.getYOffset());
            BlockState offsetState = ChunkTreeHelper.getStateSafe(level, offsetPos);
            if (offsetState != null && offsetState.is(this)){
                Block offsetBlock = offsetState.getBlock();
                level.scheduleTick(offsetPos, offsetBlock, 0);
            }
        }
    }


    protected boolean canBlockStay(Level level, BlockPos pos, BlockState state) {
        final BlockPos below = pos.below();
        final BlockState belowState = level.getBlockState(below);

        final int radius = getRadius(state);

        if (belowState.isFaceSturdy(level, below, Direction.UP)) { // If a root is sitting on a solid block.
            for (Direction dir : CoordUtils.HORIZONTALS) {
                final RootConnection conn = this.getSideConnectionRadius(level, pos, dir);

                if (conn != null && conn.radius > radius) {
                    return true;
                }
            }
        } else { // If the root has no solid block under it.
            boolean connections = false;

            for (Direction dir : CoordUtils.HORIZONTALS) {
                final RootConnection conn = this.getSideConnectionRadius(level, pos, dir);

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
