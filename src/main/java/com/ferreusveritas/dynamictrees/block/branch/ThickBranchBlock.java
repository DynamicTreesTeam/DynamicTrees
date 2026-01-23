package com.ferreusveritas.dynamictrees.block.branch;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.ShellOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThickBranchBlock extends BasicBranchBlock implements Musable {

    public static final int MAX_RADIUS_THICK = (TrunkShellBlock.MAX_DISTANCE*2+1)*8;
    public static final int RADIUS_TO_INNER_SHELL = 8;      // > 8 needs 3×3
    public static final int RADIUS_TO_OUTER_SHELL = 24;     // > 24 needs 5×5
    public static final int RADIUS_TO_OUTERMOST_SHELL = 40; // > 40 needs 7×7

    protected static final IntegerProperty RADIUS_DOUBLE = IntegerProperty.create("radius", 1, MAX_RADIUS_THICK);

    @Deprecated
    public ThickBranchBlock(ResourceLocation name, MapColor mapColor) {
        this(name, Properties.of().mapColor(mapColor));
    }

    public ThickBranchBlock(ResourceLocation name, Properties properties) {
        super(name, properties, RADIUS_DOUBLE, MAX_RADIUS_THICK);
    }

    public TrunkShellBlock getTrunkShell() {
        return DTRegistries.TRUNK_SHELL.get();
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS_DOUBLE).add(WATERLOGGED);
    }

///////////////////////////////////////////
// GROWTH
    ///////////////////////////////////////////

    @Override
    public int getRadius(BlockState state) {
        if (!(state.getBlock() instanceof ThickBranchBlock)) {
            return super.getRadius(state);
        }
        return isSameTree(state) ? Mth.clamp(state.getValue(RADIUS_DOUBLE), 1, getMaxRadius()) : 0;
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
        if (this.updateTrunkShells(level, pos, radius, flags)) {
            return super.setRadius(level, pos, radius, originDir, flags);
        }
        return super.setRadius(level, pos, getRadius(level.getBlockState(pos)), originDir, flags);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        updateTrunkShells(level, pos, getRadius(state), 6);
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
    }

    private boolean updateTrunkShells(LevelAccessor level, BlockPos pos, int radius, int flags) {
        boolean needsInnerRing = radius > RADIUS_TO_INNER_SHELL;       // > 8
        boolean needsOuterRing = radius > RADIUS_TO_OUTER_SHELL;       // > 24
        boolean needsOutermostRing = radius > RADIUS_TO_OUTERMOST_SHELL; // > 40

        // No shells needed
        if (!needsInnerRing) {
            return true;
        }

        // === Check inner ring ===
        final ReplaceableState[] innerRepStates = new ReplaceableState[8];
        ShellOffset[] innerDirs = ShellOffset.levelValues(1);
        for (int i = 0; i < innerDirs.length; i++) {
            ShellDirection dir = innerDirs[i];
            BlockPos dPos = pos.offset(dir.getOffset());
            ReplaceableState rep = getReplaceability(level, dPos, pos, 1);
            innerRepStates[i] = rep;
            if (rep == ReplaceableState.BLOCKING) {
                return false;
            }
        }

        // === Check outer ring (if needed) ===
        final ReplaceableState[] outerRepStates = new ReplaceableState[16];
        ShellDirection[] outerDirs = ShellDirection.outerValues();
        if (needsOuterRing) {
            for (int i = 0; i < outerDirs.length; i++) {
                ShellDirection dir = outerDirs[i];
                BlockPos dPos = pos.offset(dir.getOffset());
                ReplaceableState rep = getReplaceability(level, dPos, pos, 2);
                outerRepStates[i] = rep;
                if (rep == ReplaceableState.BLOCKING) {
                    return false;
                }
            }
        }

        // === Check outermost ring (if needed) ===
        final ReplaceableState[] outermostRepStates = new ReplaceableState[24];
        ShellDirection[] outermostDirs = ShellDirection.outermostValues();
        if (needsOutermostRing) {
            for (int i = 0; i < outermostDirs.length; i++) {
                ShellDirection dir = outermostDirs[i];
                BlockPos dPos = pos.offset(dir.getOffset());
                ReplaceableState rep = getReplaceability(level, dPos, pos, 3);
                outermostRepStates[i] = rep;
                if (rep == ReplaceableState.BLOCKING) {
                    return false;
                }
            }
        }

        // === Place shells ===
        BlockState trunkState = level.getBlockState(pos);
        boolean isWaterlogged = trunkState.hasProperty(WATERLOGGED) && trunkState.getValue(WATERLOGGED);

        // Place inner ring
        for (int i = 0; i < innerDirs.length; i++) {
            ShellDirection dir = innerDirs[i];
            BlockPos dPos = pos.offset(dir.getOffset());
            ReplaceableState rep = innerRepStates[i];
            boolean replacingWater = isWaterlogged || level.getBlockState(dPos).getFluidState() == Fluids.WATER.getSource(false);

            if (rep == ReplaceableState.REPLACEABLE) {
                level.setBlock(dPos, getTrunkShell().defaultBlockState()
                        .setValue(TrunkShellBlock.CORE_DIR, dir.getOpposite())
                        .setValue(TrunkShellBlock.WATERLOGGED, replacingWater), flags);
            }
        }

        // Place outer ring (if needed)
        if (needsOuterRing) {
            for (int i = 0; i < outerDirs.length; i++) {
                ShellDirection dir = outerDirs[i];
                BlockPos dPos = pos.offset(dir.getOffset());
                ReplaceableState rep = outerRepStates[i];
                boolean replacingWater = isWaterlogged || level.getBlockState(dPos).getFluidState() == Fluids.WATER.getSource(false);

                if (rep == ReplaceableState.REPLACEABLE) {
                    level.setBlock(dPos, getTrunkShell().defaultBlockState()
                            .setValue(TrunkShellBlock.CORE_DIR, dir.getOpposite())
                            .setValue(TrunkShellBlock.WATERLOGGED, replacingWater), flags);
                }
            }
        }

        // Place outermost ring (if needed)
        if (needsOutermostRing) {
            for (int i = 0; i < outermostDirs.length; i++) {
                ShellDirection dir = outermostDirs[i];
                BlockPos dPos = pos.offset(dir.getOffset());
                ReplaceableState rep = outermostRepStates[i];
                boolean replacingWater = isWaterlogged || level.getBlockState(dPos).getFluidState() == Fluids.WATER.getSource(false);

                if (rep == ReplaceableState.REPLACEABLE) {
                    level.setBlock(dPos, getTrunkShell().defaultBlockState()
                            .setValue(TrunkShellBlock.CORE_DIR, dir.getOpposite())
                            .setValue(TrunkShellBlock.WATERLOGGED, replacingWater), flags);
                }
            }
        }

        return true;
    }

    public ReplaceableState getReplaceability(LevelAccessor level, BlockPos pos, BlockPos corePos, int ringLevel) {
        final BlockState state = level.getBlockState(pos);
        final Block block = state.getBlock();

        if (block instanceof TrunkShellBlock) {
            ShellDirection dir = state.getValue(TrunkShellBlock.CORE_DIR);
            return pos.offset(dir.getOffset()).equals(corePos) ? ReplaceableState.SHELL : ReplaceableState.BLOCKING;
        }

        if (state.canBeReplaced() || state.is(DTBlockTags.FOLIAGE)) {
            return ReplaceableState.REPLACEABLE;
        }

        if (TreeHelper.isTreePart(block)) {
            return ReplaceableState.TREEPART;
        }

        if (block instanceof SurfaceRootBlock) {
            return ReplaceableState.TREEPART;
        }

        if (BranchConnectables.isBlockConnectable(block)) {
            return ReplaceableState.TREEPART;
        }

        if (block instanceof FruitBlock || block instanceof PodBlock) {
            return ReplaceableState.TREEPART;
        }

        if (this.getFamily().getCommonSpecies().isAcceptableSoilForWorldgen(level, pos, state)) {
            return ReplaceableState.REPLACEABLE;
        }

        if (ringLevel == 1) {
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0 && hardness < 1) {
                return ReplaceableState.REPLACEABLE;
            }
        }

        if (ringLevel == 2) {
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0 && hardness < 3) {
                return ReplaceableState.REPLACEABLE;
            }
        }

        // Outermost ring can break most hard blocks
        if (ringLevel == 3) {
            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0 && hardness < 5) {
                return ReplaceableState.REPLACEABLE;
            }
        }

        return ReplaceableState.BLOCKING;
    }

    enum ReplaceableState {
        SHELL,
        REPLACEABLE,
        BLOCKING,
        TREEPART
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        if (from instanceof ThickBranchBlock) {
            return getRadius(state);
        }
        return Math.min(getRadius(state), MAX_RADIUS);
    }

    @Override
    protected int getSideConnectionRadius(BlockGetter level, BlockPos pos, int radius, Direction side) {
        final BlockPos deltaPos = pos.relative(side);
        final BlockState blockState = CoordUtils.getStateSafe(level, deltaPos);

        if (blockState == null) {
            return 0;
        }

        final int connectionRadius = TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, level, deltaPos, this, side, radius);
        return Math.min(MAX_RADIUS, connectionRadius);
    }

    @Override
    public int getMaxRadius() {
        return MAX_RADIUS_THICK;
    }

///////////////////////////////////////////
// PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final int thisRadius = getRadius(state);
        if (thisRadius <= MAX_RADIUS) {
            return super.getShape(state, level, pos, context);
        }

        final double radius = thisRadius / 16.0;
        return Shapes.create(new AABB(0.5 - radius, 0.0, 0.5 - radius, 0.5 + radius, 1.0, 0.5 + radius));
    }

    @Override
    public boolean isMusable(BlockGetter level, BlockState state, BlockPos pos) {
        return getRadius(state) > 8;
    }
}
