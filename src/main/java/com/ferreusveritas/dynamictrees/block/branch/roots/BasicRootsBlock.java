package com.ferreusveritas.dynamictrees.block.branch.roots;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.cell.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

public class BasicRootsBlock extends BranchBlock implements SimpleWaterloggedBlock {
    public static final String NAME_SUFFIX = "_roots";

    public static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
    public static final EnumProperty<Layer> LAYER = EnumProperty.create("layer", Layer.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public enum Layer implements StringRepresentable {
        EXPOSED (MangroveFamily::getPrimitiveRoots),
        FILLED (MangroveFamily::getPrimitiveFilledRoots),
        COVERED (MangroveFamily::getPrimitiveCoveredRoots);
        final Function<MangroveFamily, Optional<Block>> primitiveFunc;
        Layer(Function<MangroveFamily, Optional<Block>> primitiveFunc){
            this.primitiveFunc = primitiveFunc;
        }
        @Override public @NotNull String getSerializedName() {
            return toString().toLowerCase();
        }

        public Optional<Block> getPrimitive (MangroveFamily family){
            return primitiveFunc.apply(family);
        }
    }

    private int flammability = 5; // Mimic vanilla logs
    private int fireSpreadSpeed = 5; // Mimic vanilla logs

    public BasicRootsBlock(ResourceLocation name, BlockBehaviour.Properties properties) {
        super(name, properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(LAYER, Layer.EXPOSED));
    }

    public boolean isFullBlock (BlockState state){
        return state.getValue(LAYER) == Layer.COVERED;
    }
    public MangroveFamily getFamily() {
        return (MangroveFamily) super.getFamily();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        int radius = getRadius(level.getBlockState(pos));
        return (fireSpreadSpeed * radius) / 8;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return flammability;
    }

    public BasicRootsBlock setFlammability(int flammability) {
        this.flammability = flammability;
        return this;
    }

    public BasicRootsBlock setFireSpreadSpeed(int fireSpreadSpeed) {
        this.fireSpreadSpeed = fireSpreadSpeed;
        return this;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS, LAYER, WATERLOGGED);
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return 0;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return getRadius(state);
    }

    @Override
    public int getRadius(BlockState state) {
        return state.getValue(RADIUS);
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        return signal;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public boolean checkForRot(LevelAccessor level, BlockPos pos, Species species, int fertility, int radius, RandomSource rand, float chance, boolean rapid) {
        return false;
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @javax.annotation.Nullable Direction originDir, int flags) {
        destroyMode = DynamicTrees.DestroyMode.SET_RADIUS;
//        Layer loggedState = Layer.EXPOSED;
//        BlockState state = level.getBlockState(pos);
//        if (state.getBlock() instanceof BasicRootsBlock)
//            loggedState = state.getValue(LAYER);
//        if (state.is(Blocks.DIRT))
//            loggedState = GroundLogged.DIRT;
//        else if (state.is(Blocks.GRASS_BLOCK))
//            loggedState = GroundLogged.GRASS;
//        else if (state.getFluidState() == Fluids.WATER.getSource(false) && radius <= maxRadiusForWaterLogging)
//            loggedState = GroundLogged.WATER;
        level.setBlock(pos, getStateForRadius(radius), flags);
        destroyMode = DynamicTrees.DestroyMode.SLOPPY;
        return radius;
    }

    @Override
    public BlockState getStateForRadius(int radius) {
        return defaultBlockState().setValue(RADIUS, radius);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (isFullBlock(state) && getFamily().getPrimitiveCoveredRoots().isPresent()){
            return new ItemStack(getFamily().getPrimitiveCoveredRoots().get());
        }
        return new ItemStack(asItem());
    }

    //////////////////////////////
    // SOUNDS
    //////////////////////////////

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<Block> primitive = state.getValue(LAYER).getPrimitive(getFamily());
        if (primitive.isPresent()){
            return primitive.get().getSoundType(state, level, pos, entity);
        } else
            return super.getSoundType(state, level, pos, entity);
    }


    //////////////////////////////
    // WATER LOGGING
    //////////////////////////////

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        return !isFullBlock(pState)
                && !pState.getValue(BlockStateProperties.WATERLOGGED)
                && pFluid == Fluids.WATER;
    }

    @Override
    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (canPlaceLiquid(pLevel, pPos, pState, pFluidState.getType())) {
            if (!pLevel.isClientSide()) {
                pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, true).setValue(LAYER, Layer.EXPOSED), 3);
                pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            }

            return true;
        } else {
            return false;
        }
    }

    //////////////////////////////
    // INTERACTION
    //////////////////////////////

    protected boolean canPlace(Player player, Level level, BlockPos clickedPos, BlockState pState) {
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return pState.canSurvive(level, clickedPos) && level.isUnobstructed(pState, clickedPos, collisioncontext);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isFullBlock(state)) {
            return InteractionResult.PASS;
        }
        ItemStack handStack = player.getItemInHand(hand);
        Block coverBlock = getFamily().getPrimitiveCoveredRoots().orElse(null);
        if (coverBlock != null && handStack.getItem() == coverBlock.asItem()){
            BlockState newState = state.setValue(LAYER, Layer.COVERED).setValue(WATERLOGGED, false);
            if (canPlace(player, level, pos, newState)){
                level.setBlock(pos, newState, 3);
                if (!player.isCreative()) handStack.shrink(1);
                level.playSound(null, pos, coverBlock.getSoundType(state, level, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1f, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {

        if (isFullBlock(state)){
            level.setBlock(pos, state.setValue(LAYER, Layer.FILLED), level.isClientSide ? 11 : 3);
            this.spawnDestroyParticles(level, player, pos, state);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            Block primitive = state.getValue(LAYER).getPrimitive(getFamily()).orElse(null);
            if (!player.isCreative() && primitive != null) dropResources(primitive.defaultBlockState(), level, pos);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    //////////////////////////////
    // SHAPE
    //////////////////////////////


    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if (pState.getValue(LAYER) == Layer.EXPOSED) return Shapes.empty();
        return super.getOcclusionShape(pState, pLevel, pPos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (isFullBlock(pState)) {
            VoxelShape fullShape = Shapes.block();
            if (getFamily().getPrimitiveCoveredRoots().isPresent())
                fullShape = getFamily().getPrimitiveCoveredRoots().get().getCollisionShape(pState, pLevel, pPos, pContext);
            return fullShape;
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        if (isFullBlock(state)) {
            return Shapes.block();
        }
        int thisRadiusInt = getRadius(state);
        double radius = thisRadiusInt / 16.0;
        VoxelShape core = Shapes.box(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);

        for (Direction dir : Direction.values()) {
            int sideRadiusInt = Math.min(getSideConnectionRadius(level, pos, thisRadiusInt, dir), thisRadiusInt);
            double sideRadius = sideRadiusInt / 16.0f;
            if (sideRadius > 0.0f) {
                double gap = 0.5f - sideRadius;
                AABB aabb = new AABB(0.5 - sideRadius, 0.5 - sideRadius, 0.5 - sideRadius, 0.5 + sideRadius, 0.5 + sideRadius, 0.5 + sideRadius);
                aabb = aabb.expandTowards(dir.getStepX() * gap, dir.getStepY() * gap, dir.getStepZ() * gap);
                core = Shapes.or(core, Shapes.create(aabb));
            }
        }

        return core;
    }

    protected int getSideConnectionRadius(BlockGetter level, BlockPos pos, int radius, Direction side) {
        final BlockPos deltaPos = pos.relative(side);
        final BlockState blockState = CoordUtils.getStateSafe(level, deltaPos);

        // If adjacent block is not loaded assume there is no connection.
        return blockState == null ? 0 : TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, level, deltaPos, this, side, radius);
    }

}
