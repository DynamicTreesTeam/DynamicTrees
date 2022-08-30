package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class DynamicSaplingBlock extends Block implements BonemealableBlock, IPlantable {

    protected Species species;

    public DynamicSaplingBlock(Species species) {
        super(Properties.of(Material.PLANT).sound(SoundType.GRASS).randomTicks().noOcclusion());
        this.species = species;
    }


    ///////////////////////////////////////////
    // TREE INFORMATION
    ///////////////////////////////////////////

    public Species getSpecies() {
        return species;
    }

    @Override
    public boolean isValidBonemealTarget(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
        return this.getSpecies().canSaplingConsumeBoneMeal((Level) world, pos);
    }

    @Override
    public boolean isBonemealSuccess(@Nonnull Level world, @Nonnull RandomSource rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.getSpecies().canSaplingGrowAfterBoneMeal(world, rand, pos);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFireSpread();
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFlammability();
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (this.getSpecies().canSaplingGrowNaturally(worldIn, pos)) {
            this.performBonemeal(worldIn, rand, pos, state);
        }
    }

    public static boolean canSaplingStay(LevelReader world, Species species, BlockPos pos) {
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
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return canSaplingStay(world, this.getSpecies(), pos);
    }

    @Override
    public void performBonemeal(@Nonnull ServerLevel world, @Nonnull RandomSource rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
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
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return this.getSpecies().getSaplingSound();
    }

    ///////////////////////////////////////////
    // DROPS
    ///////////////////////////////////////////


    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!this.canSurvive(state, world, pos)) {
            this.dropBlock(world, state, pos);
        }
    }

    protected void dropBlock(Level world, BlockState state, BlockPos pos) {
        world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getSpecies().getSeedStack(1)));
        world.removeBlock(pos, false);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return this.getSpecies().getSeedStack(1);
    }


    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter access, BlockPos pos, CollisionContext context) {
        return this.getSpecies().getSaplingShape();
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public BlockState getPlant(BlockGetter world, BlockPos pos) {
        return this.defaultBlockState();
    }

}
