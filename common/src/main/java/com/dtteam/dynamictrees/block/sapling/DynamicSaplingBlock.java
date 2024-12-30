//package com.dtteam.dynamictrees.block;
//
//import com.ferreusveritas.dynamictrees.api.TreeHelper;
//import com.ferreusveritas.dynamictrees.init.DTConfigs;
//import com.ferreusveritas.dynamictrees.tree.species.Species;
//import com.ferreusveritas.dynamictrees.util.CoordUtils;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelReader;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.BonemealableBlock;
//import net.minecraft.world.level.block.SoundType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.material.MapColor;
//import net.minecraft.world.level.material.PushReaction;
//import net.minecraft.world.level.storage.loot.LootDataType;
//import net.minecraft.world.level.storage.loot.LootParams;
//import net.minecraft.world.phys.shapes.CollisionContext;
//import net.minecraft.world.phys.shapes.VoxelShape;
//
//import org.jetbrains.annotations.Nonnull;
//import java.util.Collections;
//import java.util.List;
//
//public class DynamicSaplingBlock extends Block implements BonemealableBlock {
//
//    protected Species species;
//
//    public DynamicSaplingBlock(Species species) {
//        super(Properties.of().mapColor(MapColor.PLANT).noCollission().pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion());
//        this.species = species;
//    }
//
//
//    ///////////////////////////////////////////
//    // TREE INFORMATION
//    ///////////////////////////////////////////
//
//    public Species getSpecies() {
//        return species;
//    }
//
//    ///////////////////////////////////////////
//    // INTERACTION
//    ///////////////////////////////////////////
//
//    //Neoforge override
//    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
//        return this.getSpecies().saplingFireSpread();
//    }
//
//    //Neoforge override
//    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
//        return this.getSpecies().saplingFlammability();
//    }
//
//    @Override
//    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
//        if (this.getSpecies().canSaplingGrowNaturally(level, pos)) {
//            this.performBonemeal(level, rand, pos, state);
//        }
//    }
//
//    public static boolean canSaplingStay(LevelReader level, Species species, BlockPos pos) {
//        //Ensure there are no adjacent branches or other saplings
//        for (Direction dir : CoordUtils.HORIZONTALS) {
//            BlockState blockState = level.getBlockState(pos.relative(dir));
//            Block block = blockState.getBlock();
//            if (TreeHelper.isBranch(block) || block instanceof DynamicSaplingBlock) {
//                return false;
//            }
//        }
//
//        //Air above and acceptable soil below
//        return level.isEmptyBlock(pos.above()) && species.isAcceptableSoil(level, pos.below(), level.getBlockState(pos.below()));
//    }
//
//    @Override
//    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
//        return canSaplingStay(level, this.getSpecies(), pos);
//    }
//
//    @Override
//    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
//        return this.getSpecies().canSaplingConsumeBoneMeal(level, pos);
//    }
//
//    @Override
//    public boolean isBonemealSuccess(@Nonnull Level level, @Nonnull RandomSource rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
//        return this.getSpecies().canSaplingGrowAfterBoneMeal(level, rand, pos);
//    }
//
//    @Override
//    public void performBonemeal(@Nonnull ServerLevel level, @Nonnull RandomSource rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
//        if (this.canSurvive(state, level, pos)) {
//            final Species species = this.getSpecies().selfOrLocationOverride(level, pos);;
//            if (species.canSaplingGrow(level, pos)) {
//                species.transitionToTree(level, pos);
//            }
//        } else {
//            this.dropBlock(level, state, pos);
//        }
//    }
//
//    @Override
//    protected SoundType getSoundType(BlockState state) {
//        return this.getSpecies().getSaplingSound();
//    }
//
//    ///////////////////////////////////////////
//    // DROPS
//    ///////////////////////////////////////////
//
//    @Override
//    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
//        if (!this.canSurvive(state, level, pos)) {
//            this.dropBlock(level, state, pos);
//        }
//    }
//
//    protected void dropBlock(Level level, BlockState state, BlockPos pos) {
//        if (level instanceof ServerLevel serverLevel){
//            getDrops(state, new LootParams.Builder(serverLevel)).forEach((drop) -> popResource(level, pos, drop));
//            level.removeBlock(pos, false);
//        }
//    }
//
//    @Override
//    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
//        return this.getSpecies().getSeedStack(1);
//    }
//
//    @Nonnull
//    @Override
//    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootParams.Builder builder) {
//        // If a loot table has been added load those drops instead (until drop creators).
//        if (builder.getLevel().getServer().getLootData().getElement(LootDataType.TABLE, this.getLootTable()) != null) {
//            return super.getDrops(state, builder);
//        }
//
//        return DTConfigs.DYNAMIC_SAPLING_DROPS.get() ?
//                Collections.singletonList(this.getSpecies().getSeedStack(1)) :
//                Collections.emptyList();
//    }
//
//    ///////////////////////////////////////////
//    // PHYSICAL BOUNDS
//    ///////////////////////////////////////////
//
//    @Nonnull
//    @Override
//    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
//        return this.getSpecies().getSaplingShape();
//    }
//
//}
