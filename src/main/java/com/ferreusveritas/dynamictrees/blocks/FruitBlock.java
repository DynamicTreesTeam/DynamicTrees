package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.ferreusveritas.dynamictrees.util.ShapeUtils.createFruitShape;

@SuppressWarnings({"deprecation", "unused"})
public class FruitBlock extends Block implements BonemealableBlock {

    enum MatureFruitAction {
        NOTHING,
        DROP,
        ROT,
        CUSTOM
    }

    /**
     * Default shapes for the apple fruit, each element is the shape for each growth stage.
     */
    protected AABB[] FRUIT_AABB = new AABB[]{
            createFruitShape(1, 1, 0, 16),
            createFruitShape(1, 2, 0, 16),
            createFruitShape(2.5f, 5, 0),
            createFruitShape(2.5f, 5, 1.25f)
    };

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final Map<Species, Set<FruitBlock>> SPECIES_FRUIT_MAP = new HashMap<>();

    @Nonnull
    public static Set<FruitBlock> getFruitBlocksForSpecies(Species species) {
        return SPECIES_FRUIT_MAP.getOrDefault(species, new HashSet<>());
    }

    protected ItemStack droppedFruit = ItemStack.EMPTY;
    protected Supplier<Boolean> canBoneMeal = () -> false; // Q: Does dusting an apple with bone dust make it grow faster? A: Not by default.
    protected Vec3 itemSpawnOffset = new Vec3(0.5, 0.6, 0.5);
    private Species species;

    public FruitBlock() {
        super(Properties.of(Material.PLANT)
                .randomTicks()
                .strength(0.3f));
    }

    public FruitBlock setCanBoneMeal(boolean canBoneMeal) {
        return this.setCanBoneMeal(() -> canBoneMeal);
    }

    public FruitBlock setCanBoneMeal(Supplier<Boolean> canBoneMeal) {
        this.canBoneMeal = canBoneMeal;
        return this;
    }

    public void setItemSpawnOffset(float x, float y, float z) {
        this.itemSpawnOffset = new Vec3(Math.min(Math.max(x, 0), 1), Math.min(Math.max(y, 0), 1), Math.min(Math.max(z, 0), 1));
    }

    public void setSpecies(Species species) {
        if (SPECIES_FRUIT_MAP.containsKey(species)) {
            SPECIES_FRUIT_MAP.get(species).add(this);
        } else {
            Set<FruitBlock> set = new HashSet<>();
            set.add(this);
            SPECIES_FRUIT_MAP.put(species, set);
        }
        this.species = species;
    }

    public Species getSpecies() {
        return this.species == null ? Species.NULL_SPECIES : this.species;
    }

    public float getMinimumSeasonalValue() {
        return 0.3f;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        this.doTick(state, world, pos, rand);
    }

    public void doTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        if (this.shouldBlockDrop(world, pos, state)) {
            this.dropBlock(world, state, pos);
            return;
        }

        final int age = state.getValue(AGE);
        final Float season = SeasonHelper.getSeasonValue(world, pos);
        final Species species = this.getSpecies();

        if (season != null && species.isValid()) { // Non-Null means we are season capable.
            if (species.seasonalFruitProductionFactor(world, pos) < getMinimumSeasonalValue()) {
                this.outOfSeasonAction(world, pos); // Destroy the block or similar action.
                return;
            }
            if (age == 0 && species.testFlowerSeasonHold(season)) {
                return; // Keep fruit at the flower stage.
            }
        }

        if (age < 3) {
            final boolean doGrow = rand.nextFloat() < this.getGrowthChance(world, pos);
            final boolean eventGrow = ForgeHooks.onCropsGrowPre(world, pos, state, doGrow);
            if (season != null ? doGrow || eventGrow : eventGrow) { // Prevent a seasons mod from canceling the growth, we handle that ourselves.
                world.setBlock(pos, state.setValue(AGE, age + 1), 2);
                ForgeHooks.onCropsGrowPost(world, pos, state);
            }
        } else {
            if (age == 3) {
                switch (this.matureAction(world, pos, state, rand)) {
                    case NOTHING:
                    case CUSTOM:
                        break;
                    case DROP:
                        this.dropBlock(world, state, pos);
                        break;
                    case ROT:
                        world.setBlockAndUpdate(pos, BlockStates.AIR);
                        break;
                }
            }
        }
    }

    protected float getGrowthChance(Level world, BlockPos blockPos) {
        return 0.2f;
    }

    /**
     * Override this to make the fruit do something once it's mature.
     *
     * @param world The world
     * @param pos   The position of the fruit block
     * @param state The current blockstate of the fruit
     * @param rand  A random number generator
     * @return MatureFruitAction action to take
     */
    protected MatureFruitAction matureAction(Level world, BlockPos pos, BlockState state, RandomSource rand) {
        return MatureFruitAction.NOTHING;
    }

    protected void outOfSeasonAction(Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, BlockStates.AIR);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbor, boolean isMoving) {
        this.onNeighborChange(state, world, pos, neighbor);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (this.shouldBlockDrop(world, pos, state)) {
            this.dropBlock((Level) world, state, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (state.getValue(AGE) >= 3) {
            this.dropBlock(worldIn, state, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    protected void dropBlock(Level worldIn, BlockState state, BlockPos pos) {
        worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (state.getValue(AGE) >= 3) {
            worldIn.addFreshEntity(new ItemEntity(worldIn, pos.getX() + itemSpawnOffset.x, pos.getY() + itemSpawnOffset.y, pos.getZ() + itemSpawnOffset.z, this.getFruitDrop(fruitDropCount(state, worldIn, pos))));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return this.getFruitDrop(1);
    }

    /**
     * Checks if Leaves of any kind are above this block. Not picky.
     *
     * @param world
     * @param pos
     * @param state
     * @return True if it should drop (leaves are not above).
     */
    public boolean shouldBlockDrop(BlockGetter world, BlockPos pos, BlockState state) {
        return !(world.getBlockState(pos.above()).getBlock() instanceof LeavesBlock);
    }


    ///////////////////////////////////////////
    //BONEMEAL
    ///////////////////////////////////////////

    @Override
    public boolean isValidBonemealTarget(BlockGetter worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource rand, BlockPos pos, BlockState state) {
        return this.canBoneMeal.get();
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
        final int age = state.getValue(AGE);
        final int newAge = Mth.clamp(age + 1, 0, 3);
        if (newAge != age) {
            world.setBlock(pos, state.setValue(AGE, newAge), 2);
        }
    }


    ///////////////////////////////////////////
    //DROPS
    ///////////////////////////////////////////


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        // If a loot table has been added load those drops instead (until drop creators).
        if (builder.getLevel().getServer().getLootTables().getIds().contains(this.getLootTable())) {
            return super.getDrops(state, builder);
        }

        final List<ItemStack> drops = new ArrayList<>();

        if (state.getValue(AGE) >= 3) {
            final ItemStack toDrop = this.getFruitDrop(fruitDropCount(state, builder.getLevel(), BlockPos.ZERO));
            if (!toDrop.isEmpty()) {
                drops.add(toDrop);
            }
        }

        return drops;
    }

    public FruitBlock setDroppedItem(ItemStack stack) {
        this.droppedFruit = stack;
        return this;
    }

    //Override this for a custom item drop
    public ItemStack getFruitDrop(int count) {
        ItemStack stack = droppedFruit.copy();
        stack.setCount(count);
        return stack;
    }

    //pos could be BlockPos.ZERO
    protected int fruitDropCount(BlockState state, Level world, BlockPos pos) {
        return 1;
    }

    ///////////////////////////////////////////
    // BOUNDARIES
    ///////////////////////////////////////////

    public FruitBlock setShape(int stage, AABB boundingBox) {
        FRUIT_AABB[stage] = boundingBox;
        return this;
    }

    public FruitBlock setShape(AABB[] boundingBox) {
        FRUIT_AABB = boundingBox;
        return this;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.create(FRUIT_AABB[state.getValue(AGE)]);
    }

    ///////////////////////////////////////////
    // BLOCKSTATE
    ///////////////////////////////////////////

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(AGE, age);
    }

    public int getAgeForSeasonalWorldGen(LevelAccessor world, BlockPos pos, @Nullable Float seasonValue) {
        if (seasonValue == null) {
            return 3;
        }

        if (this.getSpecies().testFlowerSeasonHold(seasonValue)) {
            return 0; // Fruit is as the flower stage.
        }

        return Math.min(world.getRandom().nextInt(6), 3); // Half the time the fruit is fully mature.
    }

}
