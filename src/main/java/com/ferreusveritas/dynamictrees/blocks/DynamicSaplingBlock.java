package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class DynamicSaplingBlock extends Block implements IGrowable, IPlantable {

    protected Species species;

    public DynamicSaplingBlock(Species species) {
        super(Properties.of(Material.PLANT).sound(SoundType.GRASS).randomTicks());
        this.species = species;
    }


    ///////////////////////////////////////////
    // TREE INFORMATION
    ///////////////////////////////////////////

    public Species getSpecies() {
        return species;
    }

    @Override
    public boolean isValidBonemealTarget(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
        return this.getSpecies().canSaplingConsumeBoneMeal((World) world, pos);
    }

    @Override
    public boolean isBonemealSuccess(@Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.getSpecies().canSaplingGrowAfterBoneMeal(world, rand, pos);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFireSpread();
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFlammability();
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (this.getSpecies().canSaplingGrowNaturally(worldIn, pos)) {
            this.performBonemeal(worldIn, rand, pos, state);
        }
    }

    public static boolean canSaplingStay(IWorldReader world, Species species, BlockPos pos) {
        //Ensure there are no adjacent branches or other saplings
        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockState blockState = world.getBlockState(pos.relative(dir));
            Block block = blockState.getBlock();
            if (TreeHelper.isBranch(block) || block instanceof DynamicSaplingBlock) {
                return false;
            }
        }

        //Air above and acceptable soil below
        return world.isEmptyBlock(pos.above()) && species.isAcceptableSoil(world, pos.below(), world.getBlockState(pos.below()));
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return canSaplingStay(world, this.getSpecies(), pos);
    }

    @Override
    public void performBonemeal(@Nonnull ServerWorld world, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (this.canSurvive(state, world, pos)) {
            final Species species = this.getSpecies();
            if (species.canSaplingGrow(world, pos)) {
                species.transitionToTree(world, pos);
            }
        } else {
            this.dropBlock(world, state, pos);
        }
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return this.getSpecies().getSaplingSound();
    }

    ///////////////////////////////////////////
    // DROPS
    ///////////////////////////////////////////


    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!this.canSurvive(state, world, pos)) {
            this.dropBlock(world, state, pos);
        }
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
    }

    protected void dropBlock(World world, BlockState state, BlockPos pos) {
        world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getSpecies().getSeedStack(1)));
        world.removeBlock(pos, false);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return this.getSpecies().getSeedStack(1);
    }

    @Nonnull
    @Override
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        // If a loot table has been added load those drops instead (until drop creators).
        if (builder.getLevel().getServer().getLootTables().getIds().contains(this.getLootTable())) {
            return super.getDrops(state, builder);
        }

        return DTConfigs.DYNAMIC_SAPLING_DROPS.get() ?
                Collections.singletonList(this.getSpecies().getSeedStack(1)) :
                Collections.emptyList();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return this.getSpecies().getSeedStack(1);
    }


    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader access, BlockPos pos, ISelectionContext context) {
        return this.getSpecies().getSaplingShape();
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public BlockState getPlant(IBlockReader world, BlockPos pos) {
        return this.defaultBlockState();
    }

}
