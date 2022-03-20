package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTables;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public class FruitBlock extends Block implements IGrowable, GrowableBlock {

    private final Fruit fruit;

    public FruitBlock(Properties properties, Fruit fruit) {
        super(properties);
        this.fruit = fruit;

        // Reset block state definition, as we need the fruit to be set to create it properly.
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        if (fruit != null) {
            builder.add(fruit.getAgeProperty());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return fruit.getBlockShape(getAge(state));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        doTick(state, world, pos, random);
    }

    public void doTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!this.isSupported(world, pos, state)) {
            drop(world, pos, state);
            return;
        }

        final int age = getAge(state);
        final Float season = SeasonHelper.getSeasonValue(WorldContext.create(world), pos);

        if (season != null) { // Non-Null means we are season capable.
            if (fruit.isOutOfSeason(world, pos)) {
                this.outOfSeason(world, pos); // Destroy the block or similar action.
                return;
            }
            if (age == 0 && fruit.isInFlowerHoldPeriod(world, pos, season)) {
                return;
            }
        }

        if (age < fruit.getMaxAge()) {
            tryGrow(state, world, pos, random, age, season);
        } else {
            tickMature(world, pos, state);
        }
    }

    private void outOfSeason(World world, BlockPos pos) {
        world.destroyBlock(pos, false);
    }

    private void tryGrow(BlockState state, World world, BlockPos pos, Random random, int age,
                         @Nullable Float season) {
        final boolean doGrow = random.nextFloat() < getGrowthChance(world, pos);
        final boolean eventGrow = ForgeHooks.onCropsGrowPre(world, pos, state, doGrow);
        // Prevent a seasons mod from canceling the growth, we handle that ourselves.
        if (season != null ? doGrow || eventGrow : eventGrow) {
            setAge(world, pos, state, age + 1);
            ForgeHooks.onCropsGrowPost(world, pos, state);
        }
    }

    private float getGrowthChance(World world, BlockPos pos) {
        return fruit.getGrowthChance();
    }

    public void tickMature(World world, BlockPos pos, BlockState state) {
        fruit.performMatureAction(new Info(world, pos, state));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performMatureAction(IWorld world, BlockPos pos, BlockState state) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        if (!this.isSupported(world, pos, state)) {
            drop(world, pos, state);
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return isSupported(world, pos, state);
    }

    /**
     * Checks if the block is supported. An unsupported fruit block should drop.
     * <p>
     * Base implementation checks that there is a leaf block directly above the fruit block.
     *
     * @return {@code true} if this block is supported
     */
    @Override
    public boolean isSupported(IBlockReader world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.above()).getBlock() instanceof LeavesBlock;
    }

    protected void drop(World world, BlockPos pos, BlockState state) {
        world.destroyBlock(pos, true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        //If no loot table is set up, the default behaviour is to drop the fruit item if the age is max.
        if (getLootTable() == LootTables.EMPTY &&
                pState.hasProperty(fruit.getAgeProperty()) && pState.getValue(fruit.getAgeProperty()) == fruit.getMaxAge())
            return Collections.singletonList(fruit.getItemStack());
        return super.getDrops(pState, pBuilder);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return fruit.getItemStack();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                                BlockRayTraceResult hit) {
        // Drop fruit if mature.
        if (getAge(state) >= fruit.getMaxAge()) {
            drop(world, pos, state);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean isValidBonemealTarget(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
        return fruit.canBoneMeal() && getAge(state) < fruit.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        final int age = getAge(state);
        final int newAge = Math.min(age + 1, fruit.getMaxAge());
        if (newAge != age) {
            setAge(world, pos, state, newAge);
        }
    }

    private int getAge(BlockState state) {
        return state.getValue(fruit.getAgeProperty());
    }

    private void setAge(World world, BlockPos pos, BlockState state, int newAge) {
        world.setBlock(pos, state.setValue(fruit.getAgeProperty(), newAge), 2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
        return false;
    }

}
