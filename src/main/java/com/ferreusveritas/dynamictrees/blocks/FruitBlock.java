package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public class FruitBlock extends Block implements IGrowable {

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
        final Float season = SeasonHelper.getSeasonValue(world, pos);

        if (season != null) { // Non-Null means we are season capable.
            if (fruit.isOutOfSeason(world, pos)) {
                this.outOfSeason(world, pos); // Destroy the block or similar action.
                return;
            }
            if (age == 0 && fruit.isInFlowerPeriod(season)) {
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
        world.setBlockAndUpdate(pos, BlockStates.AIR);
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
        switch (fruit.getMatureAction()) {
            case DEFAULT:
                performMatureAction(world, pos, state);
                break;
            case DROP:
                drop(world, pos, state);
                break;
            case ROT:
                world.setBlockAndUpdate(pos, BlockStates.AIR);
                break;
        }
    }

    /**
     * Performs the default mature action for this fruit block. This will be called on tick if the fruit is mature and
     * {@linkplain Fruit#getMatureAction() its mature action} is set to {@linkplain Fruit.MatureAction#DEFAULT default}.
     */
    protected void performMatureAction(World world, BlockPos pos, BlockState state) {
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

    /**
     * Checks if the block is supported. An unsupported fruit block should drop.
     * <p>
     * Base implementation checks that there is a leaf block directly above the fruit block.
     *
     * @return {@code true} if this block is supported
     */
    public boolean isSupported(IBlockReader world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.above()).getBlock() instanceof LeavesBlock;
    }

    protected void drop(World world, BlockPos pos, BlockState state) {
        world.destroyBlock(pos, true);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return fruit.getItemStack();
    }

    @Override
    public boolean isValidBonemealTarget(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
        return getAge(state) < fruit.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(World world, Random random, BlockPos pos, BlockState state) {
        return fruit.canBoneMeal();
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

}
