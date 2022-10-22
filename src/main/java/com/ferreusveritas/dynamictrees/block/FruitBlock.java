package com.ferreusveritas.dynamictrees.block;

import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"deprecation", "unused"})
public class FruitBlock extends Block implements BonemealableBlock, GrowableBlock {

    private final Fruit fruit;

    public FruitBlock(Properties properties, Fruit fruit) {
        super(properties);
        this.fruit = fruit;

        // Reset block state definition, as we need the fruit to be set to create it properly.
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (fruit != null) {
            builder.add(fruit.getAgeProperty());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return fruit.getBlockShape(getAge(state));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        doTick(state, level, pos, random);
    }

    public void doTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (!this.isSupported(level, pos, state)) {
            drop(level, pos, state);
            return;
        }

        final int age = getAge(state);
        final Float season = SeasonHelper.getSeasonValue(LevelContext.create(level), pos);

        if (season != null) { // Non-Null means we are season capable.
            if (fruit.isOutOfSeason(level, pos)) {
                this.outOfSeason(level, pos); // Destroy the block or similar action.
                return;
            }
            if (age == 0 && fruit.isInFlowerHoldPeriod(level, pos, season)) {
                return;
            }
        }

        if (age < fruit.getMaxAge()) {
            tryGrow(state, level, pos, random, age, season);
        } else {
            tickMature(level, pos, state);
        }
    }

    private void outOfSeason(Level level, BlockPos pos) {
        level.destroyBlock(pos, false);
    }

    private void tryGrow(BlockState state, Level level, BlockPos pos, Random random, int age,
                         @Nullable Float season) {
        final boolean doGrow = random.nextFloat() < getGrowthChance(level, pos);
        final boolean eventGrow = ForgeHooks.onCropsGrowPre(level, pos, state, doGrow);
        // Prevent a seasons mod from canceling the growth, we handle that ourselves.
        if (season != null ? doGrow || eventGrow : eventGrow) {
            setAge(level, pos, state, age + 1);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    private float getGrowthChance(Level level, BlockPos pos) {
        return fruit.getGrowthChance();
    }

    public void tickMature(Level level, BlockPos pos, BlockState state) {
        fruit.performMatureAction(new Info(level, pos, state));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performMatureAction(LevelAccessor level, BlockPos pos, BlockState state) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        if (!this.isSupported(level, pos, state)) {
            drop(level, pos, state);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos, state);
    }

    /**
     * Checks if the block is supported. An unsupported fruit block should drop.
     * <p>
     * Base implementation checks that there is a leaf block directly above the fruit block.
     *
     * @return {@code true} if this block is supported
     */
    @Override
    public boolean isSupported(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock;
    }

    protected void drop(Level level, BlockPos pos, BlockState state) {
        level.destroyBlock(pos, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ResourceLocation resourcelocation = this.getLootTable();
        if (resourcelocation == BuiltInLootTables.EMPTY) return Collections.emptyList();
        else {
            LootContext context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            LootTable table = context.getLevel().getServer().getLootTables().get(resourcelocation);

            //If no loot table is set up, the default behaviour is to drop the fruit item if the age is max.
            if (table == LootTable.EMPTY && state.hasProperty(fruit.getAgeProperty()) && state.getValue(fruit.getAgeProperty()) == fruit.getMaxAge()) {
                return Collections.singletonList(fruit.getItemStack());
            }

            return table.getRandomItems(context);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return fruit.getItemStack();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Drop fruit if mature.
        if (getAge(state) >= fruit.getMaxAge()) {
            drop(level, pos, state);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
        return fruit.canBoneMeal() && getAge(state) < fruit.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, Random random, BlockPos pos, BlockState state) {
        final int age = getAge(state);
        final int newAge = Math.min(age + 1, fruit.getMaxAge());
        if (newAge != age) {
            setAge(level, pos, state, newAge);
        }
    }

    public int getAge(BlockState state) {
        return state.getValue(fruit.getAgeProperty());
    }

    public float getAgeAsPercentage(BlockState state) {
        return getAge(state) * 100F / getMaxAge();
    }

    public int getMaxAge() {
        return fruit.getMaxAge();
    }

    private void setAge(Level level, BlockPos pos, BlockState state, int newAge) {
        level.setBlock(pos, state.setValue(fruit.getAgeProperty(), newAge), 2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
