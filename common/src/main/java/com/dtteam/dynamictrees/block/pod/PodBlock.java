package com.dtteam.dynamictrees.block.pod;

import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.Growable;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */
public class PodBlock extends HorizontalDirectionalBlock implements BonemealableBlock, Growable {

    public static final MapCodec<PodBlock> CODEC = simpleCodec(PodBlock::new);

    protected final Pod pod;

    public PodBlock(Properties properties) {
        super(properties);
        pod = Pod.NULL;
    }
    public PodBlock(Identifier id, Properties properties, Pod pod) {
        super(properties.setId(ResourceKey.create(Registries.BLOCK, id)));
        this.pod = pod;

        // Reset block state definition, as we need the pod to be set to create it properly.
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        if (pod != null)
            builder.add(pod.getAgeProperty());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return pod.getBlockShape(getFacing(state), getAge(state));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        doTick(state, level, pos, random);
    }

    public void doTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!this.isSupported(level, pos, state)) {
            drop(level, pos, state);
            return;
        }

        final int age = getAge(state);
        final Float season = SeasonHelper.getSeasonValue(LevelContext.create(level), pos);

        if (season != null) { // Non-Null means we are season capable.
            if (pod.isOutOfSeason(level, pos)) {
                this.outOfSeason(level, pos); // Destroy the block or similar action.
                return;
            }
            if (age == 0 && pod.isInFlowerHoldPeriod(level, pos, season)) {
                return;
            }
        }

        if (age < pod.getMaxAge()) {
            tryGrow(state, level, pos, random, age, season);
        } else {
            tickMature(level, pos, state);
        }
    }

    private void outOfSeason(Level level, BlockPos pos) {
        level.destroyBlock(pos, false);
    }

    private void tryGrow(BlockState state, Level level, BlockPos pos, RandomSource random, int age,
                         @Nullable Float season) {
        final boolean doGrow = random.nextFloat() < getGrowthChance(level, pos);
        final boolean eventGrow = Services.EVENT.canCropGrow(level, pos, state, doGrow);
        // Prevent a seasons mod from canceling the growth, we handle that ourselves.
        if (season != null ? doGrow || eventGrow : eventGrow) {
            setAge(level, pos, state, age + 1);
            Services.EVENT.cropGrowPost(level, pos, state);
        }
    }

    private float getGrowthChance(Level level, BlockPos pos) {
        return pod.getGrowthChance();
    }

    public void tickMature(Level level, BlockPos pos, BlockState state) {
        pod.performMatureAction(new Info(level, pos, state));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performMatureAction(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean movedByPiston) {
        if (!this.isSupported(level, pos, state)) {
            drop(level, pos, state);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos, state);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Base implementation checks that there is a branch block of radius 8 in the facing direction.
     *
     * @return {@code true} if this block is supported
     */
    @Override
    public boolean isSupported(LevelReader level, BlockPos pos, BlockState state) {
        final BlockState branchState = level.getBlockState(pos.relative(state.getValue(FACING)));
        return TreeHelper.getBranchOpt(branchState).map(branch -> pod.isValidRadius(branch.getRadius(branchState))).orElse(false);
    }

//    @Override
//    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder level) {
//        ResourceKey<LootTable> resourcekey = this.getLootTable();
//        if (resourcekey == BuiltInLootTables.EMPTY) return Collections.emptyList();
//        else {
//            LootParams lootParams = level.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
//            LootTable loottable = lootParams.getLevel().getServer().getLootData().getLootTable(resourcelocation);
//
//            //If no loot table is set up, the default behaviour is to drop the pod item if the age is max.
//            if (loottable == LootTable.EMPTY &&
//                    pState.hasProperty(pod.getAgeProperty()) && pState.getValue(pod.getAgeProperty()) == pod.getMaxAge())
//                return Collections.singletonList(pod.getItemStack());
//
//            return loottable.getRandomItems(lootParams);
//        }
//    }


    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return pod.getItemStack();
    }

    public boolean harvest(BlockState state, Level level, BlockPos pos) {
        // Drop pod if mature.
        if (getAge(state) >= pod.getMaxAge()) {
            drop(level, pos, state);
            return true;
        }
        return false;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return harvest(state, level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return harvest(state, level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return pod.canBoneMeal() && getAge(blockState) < pod.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        final int age = getAge(state);
        final int newAge = Math.min(age + 1, pod.getMaxAge());
        if (newAge != age) {
            setAge(level, pos, state, newAge);
        }
    }

    private Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    public int getAge(BlockState state) {
        return state.getValue(pod.getAgeProperty());
    }

    public float getAgeAsPercentage(BlockState state) {
        return getAge(state) * 100F / getMaxAge();
    }

    public int getMaxAge() {
        return pod.getMaxAge();
    }

    private void setAge(Level level, BlockPos pos, BlockState state, int newAge) {
        level.setBlock(pos, state.setValue(pod.getAgeProperty(), newAge), 2);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(pod.getAgeProperty(), 0);
        BlockPos pos = context.getClickedPos();

        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(FACING, direction);
                if (state.canSurvive(context.getLevel(), pos)) {
                    return state;
                }
            }
        }

        return null;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

}
