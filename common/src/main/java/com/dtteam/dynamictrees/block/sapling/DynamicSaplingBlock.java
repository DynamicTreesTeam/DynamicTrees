package com.dtteam.dynamictrees.block.sapling;

import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicSaplingBlock extends Block implements BonemealableBlock {

    public final static Map<Block, Species> SAPLING_REPLACERS = new HashMap<>();

    protected Species species;

    public DynamicSaplingBlock(Species species) {
        super(Properties.of().mapColor(MapColor.PLANT).noCollission().pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion());
        this.species = species;
    }

    ///////////////////////////////////////////
    // TREE INFORMATION
    ///////////////////////////////////////////

    public Species getSpecies() {
        return species;
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    /** NeoForge override */
    @SuppressWarnings("unused")
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSpecies().saplingFireSpread();
    }

    /** NeoForge override */
    @SuppressWarnings("unused")
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSpecies().saplingFlammability();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.getSpecies().canSaplingGrowNaturally(level, pos)) {
            this.performBonemeal(level, random, pos, state);
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
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return this.getSpecies().canSaplingConsumeBoneMeal(levelReader, blockPos);
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource rand, @NotNull BlockPos pos, @NotNull BlockState state) {
        return this.getSpecies().canSaplingGrowAfterBoneMeal(level, rand, pos);
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource rand, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (this.canSurvive(state, level, pos)) {
            final Species species = this.getSpecies().selfOrLocationOverride(level, pos);;
            if (species.canSaplingGrow(level, pos)) {
                species.transitionToTree(level, pos);
            }
        } else {
            this.dropBlock(level, state, pos);
        }
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
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
        if (level instanceof ServerLevel serverLevel){
            getDrops(state, new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY)).forEach((drop) -> popResource(level, pos, drop));
            level.removeBlock(pos, false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return this.getSpecies().getSeedStack(1);
    }

    @Override
    public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder builder) {
        // Drop nothing if sapling drops are disabled, nuthin'!
        if (!Services.CONFIG.getBoolConfig(IConfigHelper.DYNAMIC_SAPLING_DROPS))
            return Collections.emptyList();
        // If a loot table has been added load those drops instead.
        LootTable loottable = builder.getLevel().getServer().reloadableRegistries().getLootTable(getLootTable());
        if (loottable == LootTable.EMPTY)
            return Collections.singletonList(this.getSpecies().getSeedStack(1));

        return super.getDrops(state, builder);
    }

    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getSpecies().getSaplingShape();
    }

    ///////////////////////////////////////////
    // SAPLING REPLACEMENT
    ///////////////////////////////////////////

    public static void registerSaplingReplacer(BlockState state, Species species) {
        SAPLING_REPLACERS.put(state.getBlock(), species);
    }

    public static boolean shouldReplaceSaplingWhenPlaced(BlockState sapling){
        Block block = sapling.getBlock();
        if (!SAPLING_REPLACERS.containsKey(block)) return false;
        Species species = SAPLING_REPLACERS.get(block);
        return species.shouldReplaceSaplingWhenPlaced(sapling);
    }

    public static boolean shouldReplaceSaplingWhenGrown(BlockState sapling){
        Block block = sapling.getBlock();
        if (!SAPLING_REPLACERS.containsKey(block)) return false;
        Species species = SAPLING_REPLACERS.get(block);
        return species.shouldReplaceSaplingWhenGrown(sapling);
    }


}
