package com.ferreusveritas.dynamictrees.block;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import java.util.Random;

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
    public boolean isValidBonemealTarget(@Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
        return this.getSpecies().canSaplingConsumeBoneMeal((Level) level, pos);
    }

    @Override
    public boolean isBonemealSuccess(@Nonnull Level level, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return this.getSpecies().canSaplingGrowAfterBoneMeal(level, rand, pos);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFireSpread();
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return this.getSpecies().saplingFlammability();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        if (this.getSpecies().canSaplingGrowNaturally(level, pos)) {
            this.performBonemeal(level, rand, pos, state);
        }
    }

    public static boolean canSaplingStay(LevelReader level, Species species, BlockPos pos) {
        //Ensure there are no adjacent branches or other saplings
        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockState blockState = level.getBlockState(pos.relative(dir));
            Block block = blockState.getBlock();
            if (TreeHelper.isBranch(block) || block instanceof DynamicSaplingBlock) {
                return false;
            }
        }

        //Air above and acceptable soil below
        return level.isEmptyBlock(pos.above()) && species.isAcceptableSoil(level, pos.below(), level.getBlockState(pos.below()));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSaplingStay(level, this.getSpecies(), pos);
    }

    @Override
    public void performBonemeal(@Nonnull ServerLevel level, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (this.canSurvive(state, level, pos)) {
            final Species species = this.getSpecies();
            if (species.canSaplingGrow(level, pos)) {
                species.transitionToTree(level, pos);
            }
        } else {
            this.dropBlock(level, state, pos);
        }
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return this.getSpecies().getSaplingSound();
    }

    ///////////////////////////////////////////
    // DROPS
    ///////////////////////////////////////////


    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!this.canSurvive(state, level, pos)) {
            this.dropBlock(level, state, pos);
        }
    }

    protected void dropBlock(Level level, BlockState state, BlockPos pos) {
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getSpecies().getSeedStack(1)));
        level.removeBlock(pos, false);
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return this.getSpecies().getSeedStack(1);
    }


    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getSpecies().getSaplingShape();
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        return this.defaultBlockState();
    }

}
